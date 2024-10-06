package dev.rebel.chatmate.services;

import dev.rebel.chatmate.api.models.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream.LivestreamStatus;
import dev.rebel.chatmate.api.publicObjects.status.PublicApiStatus.ApiStatus;
import dev.rebel.chatmate.api.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.stores.LivestreamApiStore;
import dev.rebel.chatmate.util.ApiPoller;
import dev.rebel.chatmate.util.ApiPoller.PollType;
import dev.rebel.chatmate.util.ApiPollerFactory;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class StatusService {
  private final static long INTERVAL = 5000;

  private final StreamerEndpointProxy streamerEndpointProxy;
  private final ApiPoller<GetStatusResponseData> apiPoller;
  private final LivestreamApiStore livestreamApiStore;

  private @Nullable GetStatusResponseData lastStatusResponse;

  public StatusService(StreamerEndpointProxy streamerEndpointProxy, ApiPollerFactory apiPollerFactory, LivestreamApiStore livestreamApiStore) {
    this.streamerEndpointProxy = streamerEndpointProxy;
    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, this::onApiError, this::onMakeRequest, INTERVAL, PollType.CONSTANT_INTERVAL, null, null, true);
    this.livestreamApiStore = livestreamApiStore;

    this.lastStatusResponse = null;
  }

  public @Nullable PublicLivestreamStatus getLivestreamStatus() {
    if (this.lastStatusResponse == null) {
      return null;
    } else {
      return this.lastStatusResponse.livestreamStatus;
    }
  }

  public SimpleStatus getYoutubeSimpleStatus() {
    @Nullable GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.livestreamStatus.youtubeLivestream == null) {
      return SimpleStatus.OK_NO_LIVESTREAM;
    } else if (status.youtubeApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus.youtubeLivestream.status == LivestreamStatus.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public SimpleStatus getTwitchSimpleStatus() {
    @Nullable GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.livestreamStatus.twitchLivestream == null) {
      return SimpleStatus.OK_OFFLINE;
    } else if (status.twitchApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus.twitchLivestream.status == LivestreamStatus.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public SimpleStatus getAggregateSimpleStatus() {
    @Nullable GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.youtubeApiStatus.status == ApiStatus.Error || status.twitchApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus.isYoutubeLive() || status.livestreamStatus.isTwitchLive()) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public @Nullable Integer getYoutubeLiveViewerCount() {
    @Nullable GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return null;
    } else {
      return status.livestreamStatus.youtubeLiveViewers;
    }
  }

  public @Nullable Integer getTwitchLiveViewerCount() {
    @Nullable GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
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

  private void onMakeRequest(Consumer<GetStatusResponseData> onResponse, Consumer<Throwable> onError) {
    this.streamerEndpointProxy.getStatusAsync(onResponse, onError, false);
  }

  private void onApiResponse(GetStatusResponseData response) {
    // reload livestreams if something's changed
    if (this.lastStatusResponse != null) {
      PublicLivestreamStatus prevStatus = this.lastStatusResponse.livestreamStatus;
      PublicLivestreamStatus newStatus = response.livestreamStatus;

      @Nullable LivestreamStatus prevYoutubeStatus = prevStatus.youtubeLivestream == null ? null : prevStatus.youtubeLivestream.status;
      @Nullable LivestreamStatus newYoutubeStatus = newStatus.youtubeLivestream == null ? null : newStatus.youtubeLivestream.status;
      @Nullable LivestreamStatus prevTwitchStatus = prevStatus.twitchLivestream == null ? null : prevStatus.twitchLivestream.status;
      @Nullable LivestreamStatus newTwitchStatus = newStatus.twitchLivestream == null ? null : newStatus.twitchLivestream.status;
      if (prevYoutubeStatus != newYoutubeStatus || prevTwitchStatus != newTwitchStatus) {
        this.livestreamApiStore.clear();
      }
    }

    this.lastStatusResponse = response;
  }

  private void onApiError(Throwable error) {
    this.lastStatusResponse = null;
  }

  public enum SimpleStatus {
    SERVER_UNREACHABLE,
    PLATFORM_UNREACHABLE,
    OK_OFFLINE,
    OK_NO_LIVESTREAM,
    OK_LIVE
  }
}
