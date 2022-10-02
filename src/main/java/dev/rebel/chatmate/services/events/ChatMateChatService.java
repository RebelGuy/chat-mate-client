package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.api.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.services.DateTimeService;
import dev.rebel.chatmate.services.DateTimeService.UnitOfTime;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.ChatMateChatService.EventType;
import dev.rebel.chatmate.services.events.models.NewChatEventData;
import dev.rebel.chatmate.services.util.TaskWrapper;
import dev.rebel.chatmate.util.ApiPoller;
import dev.rebel.chatmate.util.ApiPoller.PollType;
import dev.rebel.chatmate.util.ApiPollerFactory;
import jline.internal.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class ChatMateChatService extends EventServiceBase<EventType> {
  private final static long TIMEOUT_WAIT = 20 * 1000;

  private final ChatEndpointProxy chatEndpointProxy;
  private final ApiPoller<GetChatResponseData> apiPoller;
  private final Config config;
  private final DateTimeService dateTimeService;

  public ChatMateChatService(LogService logService, ChatEndpointProxy chatEndpointProxy, ApiPollerFactory apiPollerFactory, Config config, DateTimeService dateTimeService) {
    super(EventType.class, logService);

    this.chatEndpointProxy = chatEndpointProxy;
    this.apiPoller = apiPollerFactory.Create(this::onApiResponse, null, this::onMakeRequest, 500, PollType.CONSTANT_PADDING, TIMEOUT_WAIT);
    this.config = config;
    this.dateTimeService = dateTimeService;
  }

  public void onNewChat(Consumer<PublicChatItem[]> callback, Object key) {
    // todo: lambdas are forbidden... will have to deal with it in a similar way to the config subscriptions
    Function<NewChatEventData.In, NewChatEventData.Out> handler = newChat -> {
      callback.accept(newChat.chatItems);
      return new NewChatEventData.Out();
    };
    super.addListener(EventType.NEW_CHAT, handler, null, key);
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
    for (EventHandler<NewChatEventData.In, NewChatEventData.Out, NewChatEventData.Options> handler : this.getListeners(EventType.NEW_CHAT, NewChatEventData.class)) {
      NewChatEventData.In eventIn = new NewChatEventData.In(response.chat);
      super.safeDispatch(EventType.NEW_CHAT, handler, eventIn);
    }
  }

  public enum EventType {
    NEW_CHAT
  }
}
