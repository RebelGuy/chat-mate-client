package dev.rebel.chatmate.events;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.publicObjects.event.*;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.publicObjects.event.PublicChatMateEvent.ChatMateEventType;
import dev.rebel.chatmate.api.models.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.events.models.NewViewerEventData;
import dev.rebel.chatmate.services.DateTimeService;
import dev.rebel.chatmate.services.DateTimeService.UnitOfTime;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.models.DonationEventData;
import dev.rebel.chatmate.events.models.LevelUpEventData;
import dev.rebel.chatmate.events.models.NewTwitchFollowerEventData;
import dev.rebel.chatmate.util.ApiPoller;
import dev.rebel.chatmate.util.ApiPoller.PollType;
import dev.rebel.chatmate.util.ApiPollerFactory;
import dev.rebel.chatmate.util.Objects;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.function.Consumer;

public class ChatMateEventService extends EventServiceBase<ChatMateEventType> {
  private final static long TIMEOUT_WAIT = 60 * 1000;

  private final StreamerEndpointProxy streamerEndpointProxy;
  private final LogService logService;
  private final ApiPoller<GetEventsResponseData> apiPoller;
  private final Config config;
  private final DateTimeService dateTimeService;

  public ChatMateEventService(LogService logService, StreamerEndpointProxy streamerEndpointProxy, ApiPollerFactory apiPollerFactory, Config config, DateTimeService dateTimeService) {
    super(ChatMateEventType.class, logService);
    this.streamerEndpointProxy = streamerEndpointProxy;
    this.logService = logService;
    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, this::onApiError, this::onMakeRequest, 1000L, PollType.CONSTANT_PADDING, TIMEOUT_WAIT);
    this.config = config;
    this.dateTimeService = dateTimeService;
  }

  public void onLevelUp(EventCallback<LevelUpEventData> handler) {
    this.addListener(ChatMateEventType.LEVEL_UP, 0, handler, null);
  }

  public void onNewTwitchFollower(EventCallback<NewTwitchFollowerEventData> handler) {
    this.addListener(ChatMateEventType.NEW_TWITCH_FOLLOWER, 0, handler, null);
  }

  public void onDonation(EventCallback<DonationEventData> handler) {
    this.addListener(ChatMateEventType.DONATION, 0, handler, null);
  }

  public void onNewViewer(EventCallback<NewViewerEventData> handler) {
    this.addListener(ChatMateEventType.NEW_VIEWER, 0, handler, null);
  }

  private void onMakeRequest(Consumer<GetEventsResponseData> callback, Consumer<Throwable> onError) {
    @Nullable Long sinceTimestamp = this.config.getLastGetChatMateEventsResponseEmitter().get();
    long lastAllowedTimestamp = this.dateTimeService.nowPlus(UnitOfTime.HOUR, -1.0);
    if (sinceTimestamp == 0) {
      sinceTimestamp = null;
    } else if (sinceTimestamp < lastAllowedTimestamp) {
      sinceTimestamp = this.dateTimeService.now();
    }
    this.streamerEndpointProxy.getEventsAsync(callback, onError, sinceTimestamp);
  }

  private void onApiResponse(GetEventsResponseData response) {
    this.config.getLastGetChatMateEventsResponseEmitter().set(response.reusableTimestamp);
    for (PublicChatMateEvent event : response.events) {
      if (event.type == ChatMateEventType.LEVEL_UP) {
        ChatMateEventType eventType = ChatMateEventType.LEVEL_UP;
        for (EventHandler<LevelUpEventData, ?> handler : this.getListeners(eventType, LevelUpEventData.class)) {
          PublicLevelUpData data = event.levelUpData;
          LevelUpEventData eventData = new LevelUpEventData(new Date(event.timestamp), data.user, data.oldLevel, data.newLevel);
          this.safeDispatch(eventType, handler, new Event<>(eventData));
        }

      } else if (event.type == ChatMateEventType.NEW_TWITCH_FOLLOWER) {
        ChatMateEventType eventType = ChatMateEventType.NEW_TWITCH_FOLLOWER;
        for (EventHandler<NewTwitchFollowerEventData, ?> handler : this.getListeners(eventType, NewTwitchFollowerEventData.class)) {
          PublicNewTwitchFollowerData data = event.newTwitchFollowerData;
          NewTwitchFollowerEventData eventData = new NewTwitchFollowerEventData(new Date(event.timestamp), data.displayName);
          this.safeDispatch(eventType, handler, new Event<>(eventData));
        }

      } else if (event.type == ChatMateEventType.DONATION) {
        ChatMateEventType eventType = ChatMateEventType.DONATION;
        for (EventHandler<DonationEventData, ?> handler : this.getListeners(eventType, DonationEventData.class)) {
          PublicDonationData data = event.donationData;
          DonationEventData eventData = new DonationEventData(new Date(event.timestamp), data);
          this.safeDispatch(eventType, handler, new Event<>(eventData));
        }

      } else if (event.type == ChatMateEventType.NEW_VIEWER) {
        ChatMateEventType eventType = ChatMateEventType.NEW_VIEWER;
        for (EventHandler<NewViewerEventData, ?> handler : this.getListeners(eventType, NewViewerEventData.class)) {
          PublicNewViewerData data = event.newViewerData;
          NewViewerEventData eventData = new NewViewerEventData(new Date(event.timestamp), data);
          this.safeDispatch(eventType, handler, new Event<>(eventData));
        }
      } else {
        this.logService.logError("Invalid ChatMate event of type " + event.type);
      }
    }
  }

  private void onApiError(Throwable error) {
    // if there was a 500 error, we can assume that sending the same request will result in the same error.
    // avoid this by resetting the timestamp from which to get events - this might mean that we miss events, but that's ok.
    if (Objects.ifClass(ChatMateApiException.class, error, e -> e.apiResponseError.errorCode == 500)) {
      this.config.getLastGetChatMateEventsResponseEmitter().set(new Date().getTime());
      this.logService.logWarning(this, "API status code was 500. To prevent further issues, the timestamp for the next request has been reset.");
    }
  }
}
