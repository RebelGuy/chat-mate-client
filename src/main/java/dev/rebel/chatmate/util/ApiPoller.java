package dev.rebel.chatmate.util;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.util.EnumHelpers;
import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ApiPoller<D> {
  private final LogService logService;
  private final Config config;
  private final Consumer<D> callback;
  @Nullable private final Consumer<Throwable> errorHandler;
  private final BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint;
  private final long interval;
  private final PollType type;
  private final Consumer<Boolean> onChatMateEnabledChanged = this::onChatMateEnabledChanged;

  private @Nullable Timer timer;

  public ApiPoller(LogService logService,
                   Config config,
                   Consumer<D> callback,
                   @Nullable Consumer<Throwable> errorHandler,
                   BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint,
                   long interval,
                   PollType type) {
    this.logService = logService;
    this.config = config;
    this.callback = callback;
    this.errorHandler = errorHandler;
    this.endpoint = endpoint;
    this.interval = interval;
    this.type = type;

    this.timer = null;

    this.config.getChatMateEnabledEmitter().onChange(this.onChatMateEnabledChanged, this);
  }

  private void onChatMateEnabledChanged(Boolean enabled) {
    if (enabled) {
      if (this.timer == null) {
        this.timer = new Timer();
        if (this.type == PollType.CONSTANT_PADDING) {
          this.timer.schedule(new TaskWrapper(this::pollApi), 0);
        } else if (this.type == PollType.CONSTANT_INTERVAL) {
          this.timer.scheduleAtFixedRate(new TaskWrapper(this::pollApi), 0, 5000);
        } else {
          throw EnumHelpers.<PollType>assertUnreachable(this.type);
        }
      }
    } else {
      if (this.timer != null) {
        this.timer.cancel();
        this.timer = null;
      }
    }
  }

  private void pollApi() {
    this.endpoint.accept(this::onApiResponse, this::onApiError);
  }

  private void onApiResponse(D data) {
    this.onHandleCallback(data, this.callback);
  }

  private void onApiError(Throwable error) {
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

    if (this.type == PollType.CONSTANT_PADDING && this.timer != null) {
      this.timer.schedule(new TaskWrapper(this::pollApi), this.interval);
    }
  }

  public enum PollType { CONSTANT_INTERVAL, CONSTANT_PADDING }
}
