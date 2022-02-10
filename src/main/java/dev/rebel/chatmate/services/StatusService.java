package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.chatMate.GetStatusResponse;
import dev.rebel.chatmate.models.chatMate.GetStatusResponse.ApiStatus;
import dev.rebel.chatmate.models.chatMate.GetStatusResponse.LivestreamStatus;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Timer;

public class StatusService {
  private final Config config;
  private final ChatMateEndpointProxy chatMateEndpointProxy;

  private @Nullable Timer timer;
  private @Nullable GetStatusResponse lastSuccessfulStatusResponse;
  private @Nullable GetStatusResponse lastStatusResponse;

  public StatusService(Config config, ChatMateEndpointProxy chatMateEndpointProxy) {
    this.config = config;
    this.chatMateEndpointProxy = chatMateEndpointProxy;

    this.timer = null;
    this.lastSuccessfulStatusResponse = null;
    this.lastStatusResponse = null;

    this.config.getChatMateEnabledEmitter().onChange(chatMateEnabled -> {
      if (chatMateEnabled) {
        this.start();
      } else {
        this.stop();
      }
    }, this);
  }

  public SimpleStatus getSimpleStatus() {
    GetStatusResponse status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.apiStatus.status == ApiStatus.Status.Error) {
      return SimpleStatus.YOUTUBE_UNREACHABLE;
    } else if (status.livestreamStatus.status == LivestreamStatus.Status.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public @Nullable Integer getLiveViewerCount() {
    GetStatusResponse status = this.lastStatusResponse;

    if (status == null) {
      return null;
    } else {
      return status.livestreamStatus.liveViewers;
    }
  }

  private void start() {
    if (this.timer == null) {
      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(new TaskWrapper(this::fetchStatus), 0, 5000);
    }
  }

  private void stop() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }
  }

  private void fetchStatus() {
    GetStatusResponse response = null;
    try {
      response = this.chatMateEndpointProxy.getStatus();
    } catch (Exception ignored) { }

    this.lastStatusResponse = response;
    if (response != null) {
      this.lastSuccessfulStatusResponse = response;
    }
  }

  public enum SimpleStatus {
    SERVER_UNREACHABLE,
    YOUTUBE_UNREACHABLE,
    OK_OFFLINE,
    OK_LIVE
  }
}
