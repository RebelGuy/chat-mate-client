package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.models.publicObjects.status.PublicApiStatus.ApiStatus;
import dev.rebel.chatmate.models.publicObjects.status.PublicLivestreamStatus.LivestreamStatus;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Timer;

public class StatusService {
  private final Config config;
  private final ChatMateEndpointProxy chatMateEndpointProxy;

  private @Nullable Timer timer;
  private @Nullable GetStatusResponseData lastSuccessfulStatusResponse;
  private @Nullable GetStatusResponseData lastStatusResponse;

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
    });
  }

  public SimpleStatus getYoutubeSimpleStatus() {
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.youtubeApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus == null) {
      return SimpleStatus.OK_OFFLINE; // todo
    } else if (status.livestreamStatus.status == LivestreamStatus.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public SimpleStatus getTwitchSimpleStatus() {
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.twitchApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus == null) {
      return SimpleStatus.OK_OFFLINE; // todo
    } else if (status.livestreamStatus.status == LivestreamStatus.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public SimpleStatus getAggregateSimpleStatus() {
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.youtubeApiStatus.status == ApiStatus.Error || status.twitchApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus == null) {
      return SimpleStatus.OK_OFFLINE; // todo
    } else if (status.livestreamStatus.status == LivestreamStatus.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public @Nullable Integer getYoutubeLiveViewerCount() {
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null || status.livestreamStatus == null) {
      return null;
    } else {
      return status.livestreamStatus.youtubeLiveViewers;
    }
  }

  public @Nullable Integer getTwitchLiveViewerCount() {
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null || status.livestreamStatus == null) {
      return null;
    } else {
      return status.livestreamStatus.twitchLiveViewers;
    }
  }

  public @Nullable Integer getTotalLiveViewerCount() {
    Integer yt = this.getYoutubeLiveViewerCount();
    Integer twitch = this.getTwitchLiveViewerCount();

    if (yt == null && twitch == null) {
      return null;
    } else if (twitch == null) {
      return yt;
    } else if (yt == null) {
      return twitch;
    } else {
      return yt + twitch;
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
    GetStatusResponseData response = null;
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
    PLATFORM_UNREACHABLE,
    OK_OFFLINE,
    OK_LIVE
  }
}
