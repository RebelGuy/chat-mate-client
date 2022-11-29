package dev.rebel.chatmate.util;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.HttpException;
import dev.rebel.chatmate.config.Config.LoginInfo;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.models.ConfigEventData;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.rebel.chatmate.util.Objects.casted;
import static dev.rebel.chatmate.util.Objects.ifClass;

public class ApiPoller<D> {
  private final LogService logService;
  private final Config config;
  private final Consumer<D> callback;
  @Nullable private final Consumer<Throwable> errorHandler;
  private final BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint;
  private final long interval;
  private final PollType type;
  private final Long timeoutWaitTime;
  private boolean requestInProgress;

  // if unauthorised, we pause the poller until the login token has been changed
  private boolean isUnauthorised;
  private @Nullable String unauthorisedLoginToken;

  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChatMateEnabledChanged = this::onChatMateEnabledChanged;
  private final Function<ConfigEventData.In<LoginInfo>, ConfigEventData.Out<LoginInfo>> _onLoginInfoChanged = this::onLoginInfoChanged;

  private @Nullable Timer timer;
  private @Nullable Long pauseUntil;

  public ApiPoller(LogService logService,
                   Config config,
                   Consumer<D> callback,
                   @Nullable Consumer<Throwable> errorHandler,
                   BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint,
                   long interval,
                   PollType type,
                   @Nullable Long timeoutWaitTime) {
    this.logService = logService;
    this.config = config;
    this.callback = callback;
    this.errorHandler = errorHandler;
    this.endpoint = endpoint;
    this.interval = interval;
    this.type = type;
    this.timeoutWaitTime = timeoutWaitTime;

    this.timer = null;
    this.pauseUntil = null;
    this.requestInProgress = false;

    this.isUnauthorised = false;
    this.unauthorisedLoginToken = null;

    this.config.getChatMateEnabledEmitter().onChange(this._onChatMateEnabledChanged, this, true);
    this.config.getLoginInfoEmitter().onChange(this._onLoginInfoChanged, this, false);
  }

  private ConfigEventData.Out<Boolean> onChatMateEnabledChanged(ConfigEventData.In<Boolean> in) {
    boolean enabled = in.data;
    if (enabled) {
      this.resumePoller();
    } else {
      this.pausePoller();
    }

    return new ConfigEventData.Out<>();
  }

  private ConfigEventData.Out<LoginInfo> onLoginInfoChanged(ConfigEventData.In<LoginInfo> in) {
    LoginInfo data = in.data;

    if (this.isUnauthorised && !java.util.Objects.equals(data.loginToken, this.unauthorisedLoginToken)) {
      this.resumePoller();
    }

    return new ConfigEventData.Out<>();
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
      this.endpoint.accept(this::onApiResponse, this::onApiError);
    } else if (this.type == PollType.CONSTANT_PADDING && this.timer != null) {
      this.timer.schedule(new TaskWrapper(this::pollApi), this.interval);
    }
  }

  private void onApiResponse(D data) {
    this.isUnauthorised = false;
    this.unauthorisedLoginToken = null;

    this.onHandleCallback(data, this.callback);
  }

  private void onApiError(Throwable error) {
    if (error instanceof ConnectException && this.timeoutWaitTime != null) {
      this.pauseUntil = new Date().getTime() + this.timeoutWaitTime;

    } else if (ifClass(HttpException.class, error, e -> e.statusCode == 502)) {
      // CHAT-368 CHAT-392 There is a known issue where the server will randomly return 502 (bad gateway) every now and then.
      // it seems that these are entirely isolated errors, so it's safe to ignore them and not show the error.
      // we could retry the request, but in the interest of avoiding additional complexity we will leave it for now.
      this.onHandleCallback(error, null);
      return;

    } else if (ifClass(ChatMateApiException.class, error, e -> e.apiResponseError.errorCode == 401)) {
      // if we aren't authorised to make the request, there's little point in trying the same request again until the
      // loginToken has been updated. the poller will be resumed automatically when the loginToken has been changed.
      this.isUnauthorised = true;
      this.unauthorisedLoginToken = casted(ChatMateApiException.class, error, e -> e.loginToken);
      this.pausePoller();

    } else {
      this.isUnauthorised = false;
      this.unauthorisedLoginToken = null;
    }

    this.onHandleCallback(error, this.errorHandler);
  }

  private <T> void onHandleCallback(T obj, @Nullable Consumer<T> callback) {
    if (callback != null) {
      try {
        callback.accept(obj);
      } catch (Exception e) {
        this.logService.logError(this, "A problem occurred while executing a response callback. Response object:", obj, "| Error:", e);
      }
    }

    onPollDone();
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
