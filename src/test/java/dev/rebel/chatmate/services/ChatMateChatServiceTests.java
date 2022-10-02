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
  @Mock Config mockConfig;
  @Mock StatefulEmitter<Long> mockLastResponseEmitter;
  @Mock DateTimeService mockDateTimeService;

  @Test
  public void newChatItem_dispatched() {
    // arrange
    PublicChatItem[] chatItems = new PublicChatItem[] { McChatServiceTests.createItem(
        McChatServiceTests.createAuthor("Test author"),
        McChatServiceTests.createText("Test text")
    )};
    GetChatResponseData chatResponse = new GetChatResponseData() {{
      reusableTimestamp = new Date().getTime();
      chat = chatItems;
    }};
    Consumer<PublicChatItem[]> mockChatSubscriber = mock(Consumer.class);
    when(this.mockConfig.getLastGetChatResponseEmitter()).thenReturn(this.mockLastResponseEmitter);
    when(this.mockLastResponseEmitter.get()).thenReturn(0L);

    // act
    ChatMateChatService chatService = new ChatMateChatService(this.mockLogService, this.mockChatEndpointProxy, this.mockApiPollerFactory, this.mockConfig, this.mockDateTimeService);
    chatService.onNewChat(mockChatSubscriber, this);

    // extract the onResponse and endpoint callbacks
    ArgumentCaptor<Consumer<GetChatResponseData>> onResponseCaptor = ArgumentCaptor.forClass(Consumer.class);
    ArgumentCaptor<BiConsumer<Consumer<GetChatResponseData>, Consumer<Throwable>>> getChatEndpointCaptor = ArgumentCaptor.forClass(BiConsumer.class);
    verify(this.mockApiPollerFactory).Create(onResponseCaptor.capture(), any(), getChatEndpointCaptor.capture(), anyLong(), any(), any());

    // simulate an endpoint call from the ApiPoller, passing through the original response callback it would have received during creation
    getChatEndpointCaptor.getValue().accept(onResponseCaptor.getValue(), null);

    // check that the actual endpoint was indeed used
    // note that the `eq()` is required because... mockito
    verify(this.mockChatEndpointProxy).getChatAsync(eq(onResponseCaptor.getValue()), any(), any(), any());

    // simulate endpoint response
    onResponseCaptor.getValue().accept(chatResponse);

    // at long last, check that any subscribers were notified of the new data
    verify(mockChatSubscriber).accept(ArgumentMatchers.argThat(arg -> arg == chatItems));
  }
}
