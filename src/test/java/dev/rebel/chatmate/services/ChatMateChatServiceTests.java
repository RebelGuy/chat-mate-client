package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.StatefulEmitter;
import dev.rebel.chatmate.models.api.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.services.events.ChatMateChatService;
import dev.rebel.chatmate.util.ApiPollerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChatMateChatServiceTests {
  @Mock LogService mockLogService;
  @Mock ChatEndpointProxy mockChatEndpointProxy;
  @Mock ApiPollerFactory mockApiPollerFactory;

  @Test
  public void newChatItem_dispatched() {
    PublicChatItem[] chatItems = new PublicChatItem[] { McChatServiceTests.createItem(
        McChatServiceTests.createAuthor("Test author"),
        McChatServiceTests.createText("Test text")
    )};
    GetChatResponseData chatResponse = new GetChatResponseData() {{
      reusableTimestamp = new Date().getTime();
      chat = chatItems;
    }};

    Consumer<PublicChatItem[]> mockSubscriber = mock(Consumer.class);

    ChatMateChatService chatService = new ChatMateChatService(this.mockLogService, this.mockChatEndpointProxy, this.mockApiPollerFactory);
    chatService.onNewChat(mockSubscriber, this);

    // extract the onResponse callback
    ArgumentCaptor<Consumer<GetChatResponseData>> onResponseCaptor = ArgumentCaptor.forClass(Consumer.class);
    ApiPollerFactory factory = verify(this.mockApiPollerFactory);
    factory.Create(onResponseCaptor.capture(), any(), any(), any(), any(), any());

    // verify that the callback methods are passed through to the endpoint proxy when executing a request
    ArgumentCaptor<BiConsumer<Consumer<GetChatResponseData>, Consumer<Throwable>>> getChatEndpointCaptor = ArgumentCaptor.forClass(BiConsumer.class);
    getChatEndpointCaptor.getValue().accept(onResponseCaptor.getValue(), null);
    verify(this.mockChatEndpointProxy).getChatAsync(onResponseCaptor.getValue(), any(), any(), any());

    // supply response to the callback
    onResponseCaptor.getValue().accept(chatResponse);

    // YtChatService calls `scheduleAtFixedRate`, which executes on a separate thread even with zero delay.
    // so wait some time until we are sure that the other thread has finished its work.
    verify(mockSubscriber).accept(ArgumentMatchers.argThat(arg -> arg == chatItems));
  }
}
