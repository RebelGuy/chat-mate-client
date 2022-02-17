package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.api.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.ChatMateChatService.EventType;
import dev.rebel.chatmate.services.events.models.NewChatEventData;
import dev.rebel.chatmate.services.util.TaskWrapper;
import jline.internal.Nullable;

import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChatMateChatService extends EventServiceBase<EventType> {
  private final Config config;
  private final ChatEndpointProxy chatEndpointProxy;

  private final long TIMEOUT_WAIT = 20 * 1000;
  private @Nullable Timer timer = null;
  private @Nullable Long lastTimestamp = null;
  private @Nullable Long pauseUntil = null;
  private Boolean requestInProgress = false;

  public ChatMateChatService(LogService logService, Config config, ChatEndpointProxy chatEndpointProxy) {
    super(EventType.class, logService);

    this.config = config;
    this.chatEndpointProxy = chatEndpointProxy;

    this.config.getChatMateEnabledEmitter().onChange(chatMateEnabled -> {
      if (chatMateEnabled) {
        this.start();
      } else {
        this.stop();
      }
    });
  }

  public void start() {
    if (this.timer == null) {
      this.timer = new Timer();
      this.timer.scheduleAtFixedRate(new TaskWrapper(this::makeRequest), 0, 500);
    }
  }

  public void stop() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }
  }

  public void onNewChat(Consumer<PublicChatItem[]> callback, Object key) {
    Function<NewChatEventData.In, NewChatEventData.Out> handler = newChat -> {
      callback.accept(newChat.chatItems);
      return new NewChatEventData.Out();
    };
    super.addListener(EventType.NEW_CHAT, handler, null, key);
  }

  private void makeRequest() {
    if (!this.canMakeRequest()) {
      return;
    }
    this.requestInProgress = true;

    GetChatResponseData response = null;
    try {
      response = this.chatEndpointProxy.getChat(this.lastTimestamp, null);
    } catch (ConnectException e) {
      this.pauseUntil = new Date().getTime() + this.TIMEOUT_WAIT;
    } catch (Exception ignored) { }

    if (response != null) {
      this.lastTimestamp = response.reusableTimestamp;
      for (EventHandler<NewChatEventData.In, NewChatEventData.Out, NewChatEventData.Options> handler : this.getListeners(EventType.NEW_CHAT, NewChatEventData.class)) {
        NewChatEventData.In eventIn = new NewChatEventData.In(response.chat);
        super.safeDispatch(EventType.NEW_CHAT, handler, eventIn);
      }
    }

    this.requestInProgress = false;
  }

  private boolean canMakeRequest() {
    boolean skipRequest = this.requestInProgress || this.pauseUntil != null && this.pauseUntil > new Date().getTime();
    return !skipRequest;
  }

  public enum EventType {
    NEW_CHAT
  }
}
