package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.publicObjects.event.PublicChatMateEvent.ChatMateEventType;
import dev.rebel.chatmate.models.api.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.models.publicObjects.event.PublicChatMateEvent;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.models.publicObjects.event.PublicLevelUpData;
import dev.rebel.chatmate.models.publicObjects.event.PublicNewTwitchFollowerData;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.models.DonationEventData;
import dev.rebel.chatmate.services.events.models.LevelUpEventData;
import dev.rebel.chatmate.services.events.models.NewTwitchFollowerEventData;
import dev.rebel.chatmate.services.util.TaskWrapper;
import dev.rebel.chatmate.util.ApiPoller;
import dev.rebel.chatmate.util.ApiPoller.PollType;
import dev.rebel.chatmate.util.ApiPollerFactory;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChatMateEventService extends EventServiceBase<ChatMateEventType> {
  private final static long TIMEOUT_WAIT = 60 * 1000;

  private final ChatMateEndpointProxy chatMateEndpointProxy;
  private final LogService logService;
  private final ApiPoller<GetEventsResponseData> apiPoller;

  private @Nullable Long lastTimestamp = null;

  public ChatMateEventService(LogService logService, ChatMateEndpointProxy chatMateEndpointProxy, ApiPollerFactory apiPollerFactory) {
    super(ChatMateEventType.class, logService);
    this.chatMateEndpointProxy = chatMateEndpointProxy;
    this.logService = logService;
    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, null, this::onMakeRequest, 1000L, PollType.CONSTANT_PADDING, TIMEOUT_WAIT);
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
    this.chatMateEndpointProxy.getEventsAsync(callback, onError, this.lastTimestamp);
  }

  private void onApiResponse(GetEventsResponseData response) {
    this.lastTimestamp = response.reusableTimestamp;
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
}
