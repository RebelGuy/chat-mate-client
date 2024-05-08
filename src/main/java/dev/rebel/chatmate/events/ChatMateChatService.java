package dev.rebel.chatmate.events;

import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.ChatMateWebsocketClient;
import dev.rebel.chatmate.api.models.websocket.Topic;
import dev.rebel.chatmate.api.models.websocket.server.EventMessageData;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.models.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.api.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.services.DateTimeService;
import dev.rebel.chatmate.services.DateTimeService.UnitOfTime;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.ChatMateChatService.EventType;
import dev.rebel.chatmate.events.models.NewChatEventData;
import dev.rebel.chatmate.util.ApiPoller;
import dev.rebel.chatmate.util.ApiPoller.PollType;
import dev.rebel.chatmate.util.ApiPollerFactory;
import dev.rebel.chatmate.util.Objects;
import jline.internal.Nullable;

import java.util.Date;
import java.util.function.Consumer;

public class ChatMateChatService extends EventServiceBase<EventType> {
  private final static long TIMEOUT_WAIT = 20 * 1000;

  private final ChatEndpointProxy chatEndpointProxy;
//  private final ApiPoller<GetChatResponseData> apiPoller;
  private final Config config;
  private final DateTimeService dateTimeService;
  private final ChatMateWebsocketClient chatMateWebsocketClient;

  public ChatMateChatService(LogService logService, ChatEndpointProxy chatEndpointProxy, ApiPollerFactory apiPollerFactory, Config config, DateTimeService dateTimeService, ChatMateWebsocketClient chatMateWebsocketClient) {
    super(EventType.class, logService);

    this.chatEndpointProxy = chatEndpointProxy;
//    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, this::onApiError, this::onMakeRequest, 500, PollType.CONSTANT_PADDING, TIMEOUT_WAIT, 2, true);
    this.config = config;
    this.dateTimeService = dateTimeService;
    this.chatMateWebsocketClient = chatMateWebsocketClient;

    this.chatMateWebsocketClient.addListener(this::onWebsocketMessage);

    // catch up on the missed events since last time
    this.onMakeRequest(this::onApiResponse, this::onApiError);
  }

  public void onNewChat(EventCallback<NewChatEventData> handler, Object key) {
    super.addListener(EventType.NEW_CHAT, 0, handler, null, key);
  }

  private void onMakeRequest(Consumer<GetChatResponseData> callback, Consumer<Throwable> onError) {
    @Nullable Long sinceTimestamp = this.config.getLastGetChatResponseEmitter().get();
    long lastAllowedTimestamp = this.dateTimeService.nowPlus(UnitOfTime.HOUR, -1.0);
    if (sinceTimestamp == 0) {
      sinceTimestamp = null;
    } else if (sinceTimestamp < lastAllowedTimestamp) {
      sinceTimestamp = this.dateTimeService.now();
    }
    this.chatEndpointProxy.getChatAsync(callback, onError, sinceTimestamp, null);
  }

  private void onApiResponse(GetChatResponseData response) {
    this.config.getLastGetChatResponseEmitter().set(response.reusableTimestamp);
    this.handleChat(response.chat);
  }

  private void handleChat(PublicChatItem[] chat) {
    Event<NewChatEventData> event = new Event<>(new NewChatEventData(chat));
    for (EventHandler<NewChatEventData, ?> handler : this.getListeners(EventType.NEW_CHAT, NewChatEventData.class)) {
      super.safeDispatch(EventType.NEW_CHAT, handler, event);
    }
  }

  private void onApiError(Throwable error) {
    // if there was a 500 error, we can assume that sending the same request will result in the same error.
    // avoid this by resetting the timestamp from which to get events - this might mean that we miss events, but that's ok.
    if (Objects.ifClass(ChatMateApiException.class, error, e -> e.apiResponseError.errorCode == 500)) {
      this.config.getLastGetChatResponseEmitter().set(new Date().getTime());
      this.logService.logWarning(this, "API status code was 500. To prevent further issues, the timestamp for the next request has been reset.");
    }
  }

  private void onWebsocketMessage(EventMessageData data) {
    if (data.topic != Topic.STREAMER_CHAT) {
      return;
    }

    PublicChatItem chatItem = data.getChatData();
    this.handleChat(new PublicChatItem[] { chatItem });
  }

  public enum EventType {
    NEW_CHAT
  }
}
