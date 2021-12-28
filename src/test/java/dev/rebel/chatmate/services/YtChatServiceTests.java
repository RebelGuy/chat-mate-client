package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.GetChatResponse;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.proxy.YtChatProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class YtChatServiceTests {
  @Mock YtChatProxy mockYtChatProxy;

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

    when(this.mockYtChatProxy.GetChat(any(), any())).thenReturn(chatResponse);
    YtChatService chatService = new YtChatService(this.mockYtChatProxy);
    Consumer<ChatItem[]> mockCallback = mock(Consumer.class);
    chatService.listen(mockCallback);

    chatService.start();

    // YtChatService calls `scheduleAtFixedRate`, which executes on a separate thread even with zero delay.
    // so wait some time until we are sure that the other thread has finished its work.
    verify(mockCallback, timeout(10)).accept(ArgumentMatchers.argThat(arg -> arg == chatItems));
  }
}
