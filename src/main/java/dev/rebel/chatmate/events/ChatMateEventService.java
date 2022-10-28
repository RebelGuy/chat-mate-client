package dev.rebel.chatmate.events;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.publicObjects.event.PublicChatMateEvent.ChatMateEventType;
import dev.rebel.chatmate.api.models.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.api.publicObjects.event.PublicChatMateEvent;
import dev.rebel.chatmate.api.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.api.publicObjects.event.PublicLevelUpData;
import dev.rebel.chatmate.api.publicObjects.event.PublicNewTwitchFollowerData;
import dev.rebel.chatmate.api.proxy.ChatMateEndpointProxy;
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
import java.util.function.Function;

public class ChatMateEventService extends EventServiceBase<ChatMateEventType> {
  private final static long TIMEOUT_WAIT = 60 * 1000;

  private final ChatMateEndpointProxy chatMateEndpointProxy;
  private final LogService logService;
  private final ApiPoller<GetEventsResponseData> apiPoller;
  private final Config config;
  private final DateTimeService dateTimeService;

  public ChatMateEventService(LogService logService, ChatMateEndpointProxy chatMateEndpointProxy, ApiPollerFactory apiPollerFactory, Config config, DateTimeService dateTimeService) {
    super(ChatMateEventType.class, logService);
    this.chatMateEndpointProxy = chatMateEndpointProxy;
    this.logService = logService;
    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, this::onApiError, this::onMakeRequest, 1000L, PollType.CONSTANT_PADDING, TIMEOUT_WAIT);
    this.config = config;
    this.dateTimeService = dateTimeService;
  }

  public void onLevelUp(Function<LevelUpEventData.In, LevelUpEventData.Out> handler, @Nullable LevelUpEventData.Options options) {
    this.addListener(ChatMateEventType.LEVEL_UP, handler, options);
  }

  public void onNewTwitchFollower(Function<NewTwitchFollowerEventData.In, NewTwitchFollowerEventData.Out> handler, @Nullable NewTwitchFollowerEventData.Options options) {
    this.addListener(ChatMateEventType.NEW_TWITCH_FOLLOWER, handler, options);
  }

  public void onDonation(Function<DonationEventData.In, DonationEventData.Out> handler, @Nullable DonationEventData.Options options) {
    this.addListener(ChatMateEventType.DONATION, handler, options);
  }

  private void onMakeRequest(Consumer<GetEventsResponseData> callback, Consumer<Throwable> onError) {
    @Nullable Long sinceTimestamp = this.config.getLastGetChatMateEventsResponseEmitter().get();
    long lastAllowedTimestamp = this.dateTimeService.nowPlus(UnitOfTime.HOUR, -1.0);
    if (sinceTimestamp == 0) {
      sinceTimestamp = null;
    } else if (sinceTimestamp < lastAllowedTimestamp) {
      sinceTimestamp = this.dateTimeService.now();
    }
    this.chatMateEndpointProxy.getEventsAsync(callback, onError, sinceTimestamp);
  }

  private void onApiResponse(GetEventsResponseData response) {
    this.config.getLastGetChatMateEventsResponseEmitter().set(response.reusableTimestamp);
    for (PublicChatMateEvent event : response.events) {
      if (event.type == ChatMateEventType.LEVEL_UP) {
        ChatMateEventType eventType = ChatMateEventType.LEVEL_UP;
        for (EventHandler<LevelUpEventData.In, LevelUpEventData.Out, LevelUpEventData.Options> handler : this.getListeners(eventType, LevelUpEventData.class)) {
          PublicLevelUpData data = event.levelUpData;
          LevelUpEventData.In eventIn = new LevelUpEventData.In(new Date(event.timestamp), data.user, data.oldLevel, data.newLevel);
          LevelUpEventData.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
        }

      } else if (event.type == ChatMateEventType.NEW_TWITCH_FOLLOWER) {
        ChatMateEventType eventType = ChatMateEventType.NEW_TWITCH_FOLLOWER;
        for (EventHandler<NewTwitchFollowerEventData.In, NewTwitchFollowerEventData.Out, NewTwitchFollowerEventData.Options> handler : this.getListeners(eventType, NewTwitchFollowerEventData.class)) {
          PublicNewTwitchFollowerData data = event.newTwitchFollowerData;
          NewTwitchFollowerEventData.In eventIn = new NewTwitchFollowerEventData.In(new Date(event.timestamp), data.displayName);
          NewTwitchFollowerEventData.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
        }

      } else if (event.type == ChatMateEventType.DONATION) {
        ChatMateEventType eventType = ChatMateEventType.DONATION;
        for (EventHandler<DonationEventData.In, DonationEventData.Out, DonationEventData.Options> handler : this.getListeners(eventType, DonationEventData.class)) {
          PublicDonationData data = event.donationData;
          DonationEventData.In eventIn = new DonationEventData.In(new Date(event.timestamp), data);
          DonationEventData.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
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
