package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.publicObjects.event.PublicChatMateEvent.ChatMateEventType;
import dev.rebel.chatmate.models.api.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.models.publicObjects.event.PublicChatMateEvent;
import dev.rebel.chatmate.models.publicObjects.event.PublicLevelUpData;
import dev.rebel.chatmate.models.publicObjects.event.PublicNewTwitchFollowerData;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.models.LevelUpEventData;
import dev.rebel.chatmate.services.events.models.NewTwitchFollowerEventData;
import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.function.Function;

public class ChatMateEventService extends EventServiceBase<ChatMateEventType> {
  private final Config config;
  private final ChatMateEndpointProxy chatMateEndpointProxy;
  private final LogService logService;
  private final long TIMEOUT_WAIT = 60 * 1000;
  private @Nullable Timer timer = null;
  private @Nullable Long lastTimestamp = null;
  private @Nullable Long pauseUntil = null;
  private boolean requestInProgress = false;

  public ChatMateEventService(LogService logService, Config config, ChatMateEndpointProxy chatMateEndpointProxy, LogService logService1) {
    super(ChatMateEventType.class, logService);
    this.config = config;
    this.chatMateEndpointProxy = chatMateEndpointProxy;
    this.logService = logService1;

    this.config.getChatMateEnabledEmitter().onChange(chatMateEnabled -> {
      if (chatMateEnabled) {
        this.start();
      } else {
        this.stop();
      }
    });
  }

  public void onLevelUp(Function<LevelUpEventData.In, LevelUpEventData.Out> handler, @Nullable LevelUpEventData.Options options) {
    this.addListener(ChatMateEventType.LEVEL_UP, handler, options);
  }

  public void onNewTwitchFollower(Function<NewTwitchFollowerEventData.In, NewTwitchFollowerEventData.Out> handler, @Nullable NewTwitchFollowerEventData.Options options) {
    this.addListener(ChatMateEventType.NEW_TWITCH_FOLLOWER, handler, options);
  }

  private void start() {
    if (this.timer == null) {
      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(new TaskWrapper(this::makeRequest), 1000, 10000);
    }
  }

  private void stop() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }
  }

  private void makeRequest() {
    if (!this.canMakeRequest()) {
      return;
    }
    this.requestInProgress = true;

    GetEventsResponseData response = null;
    try {
      response = this.chatMateEndpointProxy.getEvents(this.lastTimestamp);
    } catch (ConnectException e) {
      this.pauseUntil = new Date().getTime() + this.TIMEOUT_WAIT;
    } catch (Exception ignored) { }

    if (response == null) {
      this.requestInProgress = false;
      return;
    }

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

      } else {
        this.logService.logError("Invalid ChatMate event of type " + event.type);
      }
    }

    this.requestInProgress = false;
  }

  private boolean canMakeRequest() {
    boolean skipRequest = this.requestInProgress || this.pauseUntil != null && this.pauseUntil > new Date().getTime();
    return !skipRequest;
  }
}
