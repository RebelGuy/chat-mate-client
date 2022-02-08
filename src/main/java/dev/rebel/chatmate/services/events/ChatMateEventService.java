package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.chatMate.GetEventsResponse;
import dev.rebel.chatmate.models.chatMate.GetEventsResponse.Event;
import dev.rebel.chatmate.models.chatMate.GetEventsResponse.EventType;
import dev.rebel.chatmate.models.chatMate.GetEventsResponse.LevelUpData;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.models.LevelUpEventData;
import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.function.Function;

public class ChatMateEventService extends EventServiceBase<EventType> {
  private final Config config;
  private final ChatMateEndpointProxy chatMateEndpointProxy;
  private final long TIMEOUT_WAIT = 60 * 1000;
  private @Nullable Timer timer = null;
  private @Nullable Long lastTimestamp = null;
  private @Nullable Long pauseUntil = null;
  private boolean requestInProgress = false;

  public ChatMateEventService(LogService logService, Config config, ChatMateEndpointProxy chatMateEndpointProxy) {
    super(EventType.class, logService);
    this.config = config;
    this.chatMateEndpointProxy = chatMateEndpointProxy;

    this.config.getChatMateEnabled().listen(chatMateEnabled -> {
      if (chatMateEnabled) {
        this.start();
      } else {
        this.stop();
      }
    });
  }

  public void onLevelUp(Function<LevelUpEventData.In, LevelUpEventData.Out> handler, @Nullable LevelUpEventData.Options options) {
    this.addListener(EventType.LEVEL_UP, handler, options);
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

    GetEventsResponse response = null;
    try {
      response = this.chatMateEndpointProxy.getEvents(this.lastTimestamp);
    } catch (ConnectException e) {
      this.pauseUntil = new Date().getTime() + this.TIMEOUT_WAIT;
    } catch (Exception ignored) { }

    if (response != null) {
      EventType eventType = EventType.LEVEL_UP;
      this.lastTimestamp = response.timestamp;
      for (Event event : response.events) {
        for (EventHandler<LevelUpEventData.In, LevelUpEventData.Out, LevelUpEventData.Options> handler : this.getListeners(eventType, LevelUpEventData.class)) {
          LevelUpData data = event.getData(LevelUpData.class);
          LevelUpEventData.In eventIn = new LevelUpEventData.In(new Date(event.timestamp), data.channelName, data.oldLevel, data.newLevel);
          LevelUpEventData.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
        }
      }
    }

    this.requestInProgress = false;
  }

  private boolean canMakeRequest() {
    boolean skipRequest = this.requestInProgress || this.pauseUntil != null && this.pauseUntil > new Date().getTime();
    return !skipRequest;
  }
}
