package dev.rebel.chatmate.util;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.ApiPoller.PollType;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ApiPollerFactory {
  private final LogService logService;
  private final Config config;

  public ApiPollerFactory(LogService logService, Config config) {
    this.logService = logService;
    this.config = config;
  }

  public <D> ApiPoller<D> Create(Consumer<D> callback,
                                 @Nullable Consumer<Throwable> errorHandler,
                                 BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint,
                                 long interval,
                                 PollType type,
                                 @Nullable Long timeoutWaitTime,
                                 @Nullable Integer retries) {
    return new ApiPoller<>(this.logService, this.config, callback, errorHandler, endpoint, interval, type, timeoutWaitTime, retries);
  }
}
