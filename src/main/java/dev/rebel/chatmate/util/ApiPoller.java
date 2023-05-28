package dev.rebel.chatmate.util;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.HttpException;
import dev.rebel.chatmate.config.Config.LoginInfo;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.Objects.casted;
import static dev.rebel.chatmate.util.Objects.ifClass;

public class ApiPoller<D> {
  private final static int RETRY_DELAY = 500;

  private final LogService logService;
  private final Config config;
  private final Consumer<D> callback;
  @Nullable private final Consumer<Throwable> errorHandler;
  private final BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint;
  private final long interval;
  private final PollType type;
  private final Long timeoutWaitTime;
  private final Integer maxRetries;
  private boolean requestInProgress;
  private int currentRetries;

  // if unauthorised, we pause the poller until the login token has been changed
  private boolean isUnauthorised;
  private @Nullable String unauthorisedLoginToken;

  private final EventCallback<Boolean> _onChatMateEnabledChanged = this::onChatMateEnabledChanged;
  private final EventCallback<LoginInfo> _onLoginInfoChanged = this::onLoginInfoChanged;

  private @Nullable Timer timer;
  private @Nullable Long pauseUntil;

  public ApiPoller(LogService logService,
                   Config config,
                   Consumer<D> callback,
                   @Nullable Consumer<Throwable> errorHandler,
                   BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint,
                   long interval,
                   PollType type,
                   @Nullable Long timeoutWaitTime,
                   @Nullable Integer retries) {
    this.logService = logService;
    this.config = config;
    this.callback = callback;
    this.errorHandler = errorHandler;
    this.endpoint = endpoint;
    this.interval = interval;
    this.type = type;
    this.timeoutWaitTime = timeoutWaitTime;
    this.maxRetries = retries;

    this.timer = null;
    this.pauseUntil = null;
    this.requestInProgress = false;
    this.currentRetries = 0;

    this.isUnauthorised = false;
    this.unauthorisedLoginToken = null;

    this.config.getChatMateEnabledEmitter().onChange(this._onChatMateEnabledChanged, this, true);
    this.config.getLoginInfoEmitter().onChange(this._onLoginInfoChanged, this, false);
  }

  private void onChatMateEnabledChanged(Event<Boolean> event) {
    boolean enabled = event.getData();
    if (enabled) {
      this.resumePoller();
    } else {
      this.pausePoller();
    }
  }

  private void onLoginInfoChanged(Event<LoginInfo> in) {
    LoginInfo data = in.getData();

    if (this.isUnauthorised && !java.util.Objects.equals(data.loginToken, this.unauthorisedLoginToken)) {
      this.resumePoller();
    }
  }

  private void resumePoller() {
    if (this.timer == null) {
      this.pauseUntil = null;
      this.timer = new Timer();
      if (this.type == PollType.CONSTANT_PADDING) {
        this.timer.schedule(new TaskWrapper(this::pollApi), 0);
      } else if (this.type == PollType.CONSTANT_INTERVAL) {
        this.timer.scheduleAtFixedRate(new TaskWrapper(this::pollApi), 0, 5000);
      } else {
        throw EnumHelpers.<PollType>assertUnreachable(this.type);
      }
    }
  }

  private void pausePoller() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }
  }

  private void pollApi() {
    if (this.canMakeRequest()) {
      this.requestInProgress = true;
      this.currentRetries = 0;
      this.endpoint.accept(this::onApiResponse, this::onApiError);
    } else if (this.type == PollType.CONSTANT_PADDING && this.timer != null) {
      this.timer.schedule(new TaskWrapper(this::pollApi), this.interval);
    }
  }

  private boolean tryRetryRequest() {
    if (this.maxRetries == null) {
      return false;
    } else if (this.currentRetries >= this.maxRetries) {
      this.logService.logDebug(this, String.format("Won't retry request because the maximum number of retries (#%d) have been reached", this.maxRetries));
      return false;
    }

    this.currentRetries++;
    this.logService.logDebug(this, String.format("Retrying request in %d ms (retry #%d)", RETRY_DELAY, this.currentRetries));
    Timer timer = new Timer();
    timer.schedule(new TaskWrapper(() -> this.endpoint.accept(this::onApiResponse, this::onApiError)), RETRY_DELAY);
    return true;
  }

  private void onApiResponse(D data) {
    this.isUnauthorised = false;
    this.unauthorisedLoginToken = null;

    this.onHandleCallback(data, this.callback);
    this.onPollDone();
  }

  private void onApiError(Throwable error) {
    boolean canRetry = false;
    boolean handleCallbacks = true;

    if (error instanceof ConnectException && this.timeoutWaitTime != null) {
      this.pauseUntil = new Date().getTime() + this.timeoutWaitTime;

    } else if (ifClass(HttpException.class, error, e -> e.statusCode == 502)) {
      // CHAT-368 CHAT-392 There is a known issue where the server will randomly return 502 (bad gateway) every now and then.
      // it seems that these are entirely isolated errors, so it's safe to ignore them and not show the error.
      handleCallbacks = false;
      canRetry = true;

    } else if (ifClass(ChatMateApiException.class, error, e -> e.apiResponseError.errorCode == 401)) {
      // if we aren't authorised to make the request, there's little point in trying the same request again until the
      // loginToken has been updated. the poller will be resumed automatically when the loginToken has been changed.
      this.isUnauthorised = true;
      this.unauthorisedLoginToken = casted(ChatMateApiException.class, error, e -> e.loginToken);
      this.pausePoller();

    } else {
      this.isUnauthorised = false;
      this.unauthorisedLoginToken = null;
      canRetry = true;
    }

    if (canRetry && this.tryRetryRequest()) {
      // we are still in the same request context - don't notify callbacks or reset state
      return;
    }

    if (handleCallbacks) {
      this.onHandleCallback(error, this.errorHandler);
    }
    this.onPollDone();
  }

  private <T> void onHandleCallback(T obj, @Nullable Consumer<T> callback) {
    if (callback != null) {
      try {
        callback.accept(obj);
      } catch (Exception e) {
        this.logService.logError(this, "A problem occurred while executing a response callback. Response object:", obj, "| Error:", e);
      }
    }
  }

  private void onPollDone() {
    this.requestInProgress = false;

    if (this.type == PollType.CONSTANT_PADDING && this.timer != null) {
      this.timer.schedule(new TaskWrapper(this::pollApi), this.interval);
    }
  }

  private boolean canMakeRequest() {
    boolean skipRequest = this.requestInProgress || this.pauseUntil != null && this.pauseUntil > new Date().getTime();
    return !skipRequest;
  }

  public enum PollType { CONSTANT_INTERVAL, CONSTANT_PADDING }
}
