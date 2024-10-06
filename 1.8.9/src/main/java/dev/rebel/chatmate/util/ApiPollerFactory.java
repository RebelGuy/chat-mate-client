package dev.rebel.chatmate.util;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.stores.StreamerApiStore;
import dev.rebel.chatmate.util.ApiPoller.PollType;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ApiPollerFactory {
  private final LogService logService;
  private final Config config;
  private final StreamerApiStore streamerApiStore;

  public ApiPollerFactory(LogService logService, Config config, StreamerApiStore streamerApiStore) {
    this.logService = logService;
    this.config = config;
    this.streamerApiStore = streamerApiStore;
  }

  public <D> ApiPoller<D> Create(Consumer<D> callback,
                                 @Nullable Consumer<Throwable> errorHandler,
                                 BiConsumer<Consumer<D>, Consumer<Throwable>> endpoint,
                                 long interval,
                                 PollType type,
                                 @Nullable Long timeoutWaitTime,
                                 @Nullable Integer retries,
                                 boolean requiresStreamer) {
    return new ApiPoller<>(this.logService, this.config, this.streamerApiStore, callback, errorHandler, endpoint, interval, type, timeoutWaitTime, retries, requiresStreamer);
  }
}
