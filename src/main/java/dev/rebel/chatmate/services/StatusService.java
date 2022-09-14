package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.models.publicObjects.livestream.PublicLivestream;
import dev.rebel.chatmate.models.publicObjects.livestream.PublicLivestream.LivestreamStatus;
import dev.rebel.chatmate.models.publicObjects.status.PublicApiStatus.ApiStatus;
import dev.rebel.chatmate.models.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.util.TaskWrapper;
import dev.rebel.chatmate.store.LivestreamApiStore;
import dev.rebel.chatmate.util.ApiPoller;
import dev.rebel.chatmate.util.ApiPoller.PollType;
import dev.rebel.chatmate.util.ApiPollerFactory;

import javax.annotation.Nullable;
import java.util.Timer;
import java.util.function.Consumer;

public class StatusService {
  private final static long INTERVAL = 5000;

  private final ChatMateEndpointProxy chatMateEndpointProxy;
  private final ApiPoller<GetStatusResponseData> apiPoller;
  private final LivestreamApiStore livestreamApiStore;

  private @Nullable GetStatusResponseData lastStatusResponse;

  public StatusService(ChatMateEndpointProxy chatMateEndpointProxy, ApiPollerFactory apiPollerFactory, LivestreamApiStore livestreamApiStore) {
    this.chatMateEndpointProxy = chatMateEndpointProxy;
    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, this::onApiError, this::onMakeRequest, INTERVAL, PollType.CONSTANT_INTERVAL, null);
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
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.livestreamStatus == null) {
      return SimpleStatus.OK_NO_LIVESTREAM;
    } else if (status.youtubeApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus.livestream.status == LivestreamStatus.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public SimpleStatus getTwitchSimpleStatus() {
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.livestreamStatus == null) {
      return SimpleStatus.OK_NO_LIVESTREAM;
    } else if (status.twitchApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus.livestream.status == LivestreamStatus.Live) {
      return SimpleStatus.OK_LIVE;
    } else {
      return SimpleStatus.OK_OFFLINE;
    }
  }

  public SimpleStatus getAggregateSimpleStatus() {
    GetStatusResponseData status = this.lastStatusResponse;

    if (status == null) {
      return SimpleStatus.SERVER_UNREACHABLE;
    } else if (status.livestreamStatus == null) {
      // we show "OK" even if the platforms are unreachable because, from the client's perspective, we only care about
      // reachability during active livestreams, and it's more useful to show the OK_NO_LIVESTREAM status in this case.
      return SimpleStatus.OK_NO_LIVESTREAM;
    } else if (status.youtubeApiStatus.status == ApiStatus.Error || status.twitchApiStatus.status == ApiStatus.Error) {
      return SimpleStatus.PLATFORM_UNREACHABLE;
    } else if (status.livestreamStatus.livestream.status == LivestreamStatus.Live) {
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

  private void onMakeRequest(Consumer<GetStatusResponseData> onResponse, Consumer<Throwable> onError) {
    this.chatMateEndpointProxy.getStatusAsync(onResponse, onError, false);
  }

  private void onApiResponse(GetStatusResponseData response) {
    // reload livestreams if something's changed
    if (this.lastStatusResponse != null) {
      @Nullable PublicLivestreamStatus prevStatus = this.lastStatusResponse.livestreamStatus;
      @Nullable PublicLivestreamStatus newStatus = response.livestreamStatus;
      if ((prevStatus == null) != (newStatus == null) || prevStatus != null && newStatus != null && prevStatus.livestream.status != newStatus.livestream.status) {
        this.livestreamApiStore.invalidateStore();
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
