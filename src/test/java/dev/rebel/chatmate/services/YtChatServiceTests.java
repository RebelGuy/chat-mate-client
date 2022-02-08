package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.StatefulEmitter;
import dev.rebel.chatmate.models.chat.GetChatResponse;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.proxy.ChatEndpointProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class YtChatServiceTests {
  @Mock ChatEndpointProxy mockChatEndpointProxy;
  @Mock Config mockConfig;

  @Test
  public void newChatItem_dispatched() throws Exception {
    ChatItem[] chatItems = new ChatItem[] { McChatServiceTests.createItem(
        McChatServiceTests.createAuthor("Test author"),
        McChatServiceTests.createText("Test text")
    )};
    GetChatResponse chatResponse = new GetChatResponse() {{
      lastTimestamp = new Date().getTime();
      chat = chatItems;
    }};

    when(this.mockChatEndpointProxy.getChat(any(), any())).thenReturn(chatResponse);
    when(this.mockConfig.getChatMateEnabledEmitter()).thenReturn(new StatefulEmitter(true));
    YtChatService chatService = new YtChatService(this.mockConfig, this.mockChatEndpointProxy);
    Consumer<ChatItem[]> mockCallback = mock(Consumer.class);
    chatService.listen(mockCallback);

    chatService.start();

    // YtChatService calls `scheduleAtFixedRate`, which executes on a separate thread even with zero delay.
    // so wait some time until we are sure that the other thread has finished its work.
    verify(mockCallback, timeout(100)).accept(ArgumentMatchers.argThat(arg -> arg == chatItems));
  }
}
