package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.GetChatResponse.Author;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.models.chat.GetChatResponse.PartialChatMessage;
import dev.rebel.chatmate.models.chat.PartialChatMessageType;
import dev.rebel.chatmate.services.events.ChatMateEventService;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class) // this lets us use @Mock for auto-initialising mock objects
public class McChatServiceTests {
  @Mock MinecraftProxyService mockMinecraftProxyService;
  @Mock LogService mockLogService;
  @Mock FilterService mockFilterService;
  @Mock SoundService mockSoundService;
  @Mock ChatMateEventService mockChatMateEventService;
  @Mock MessageService mockMessageService;

  @Mock FontRenderer mockFontRenderer;

  Author author1 = createAuthor("Author 1");
  PartialChatMessage text1 = createText("Text 1");
  PartialChatMessage text2 = createText("Text 2");
  PartialChatMessage textRebel = createText("Rebel");
  PartialChatMessage emoji1 = createEmoji("☺", ":smiling:");
  PartialChatMessage emoji2 = createEmoji("slightly smiling", ":slightly_smiling:");

  @Test
  public void addChat_ignoresIfCantPrintChat() {
    McChatService service = this.setupService();
    when(this.mockMinecraftProxyService.canPrintChatMessage()).thenReturn(false);

    service.printStreamChatItem(null);

    verify(this.mockMinecraftProxyService, never()).tryPrintChatMessage(anyString(), any());
  }

  @Test
  public void addChat_usesChatFilter() {
    ChatItem item = createItem(author1, text1);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    verify(this.mockFilterService).censorNaughtyWords(text1.text);
  }

  @Test
  public void addChat_unicodeEmoji_PrintedDirectly() {
    ChatItem item = createItem(author1, emoji1);
    when(this.mockFontRenderer.getCharWidth(emoji1.name.charAt(0))).thenReturn(1);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, emoji1.name);
    verify(this.mockMinecraftProxyService).tryPrintChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_nonUnicodeEmoji_printLabel() {
    ChatItem item = createItem(author1, emoji2);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, emoji2.label);
    verify(this.mockMinecraftProxyService).tryPrintChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_multipleText_joined() {
    ChatItem item = createItem(author1, text1, text2);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, text1.text + text2.text);
    verify(this.mockMinecraftProxyService).tryPrintChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_textEmoji_spacingBetween() {
    ChatItem item = createItem(author1, text1, emoji2, emoji1, text2);
    when(this.mockFontRenderer.getCharWidth(emoji1.name.charAt(0))).thenReturn(1);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, text1.text + " " + emoji2.label + " " + emoji1.name + " " + text2.text);
    verify(this.mockMinecraftProxyService).tryPrintChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_chatMention_playsDing() {
    // so it *should* be possible to mock static methods using PowerMock by adding
    // @PrepareForTest(FilterService.class) to the method, and @RunWith(PowerMockRunner) to the class
    // but for some reason it causes everything JUnit related to break.
    // so for now just rely on the static method's implementation to work correctly (and it is tested in FilterServiceTests anyway).

    // note: if this test breaks it's probably because chat mentions are no longer hardcoded
    ChatItem item = createItem(author1, textRebel);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    verify(this.mockSoundService).playDing();
  }

  private static String getExpectedChatText(Author author, String text) {
    return author.level + " VIEWER " + author.name + " " + text;
  }

  private McChatService setupService() {
    when(this.mockMinecraftProxyService.getChatFontRenderer()).thenReturn(this.mockFontRenderer);
    when(this.mockMinecraftProxyService.canPrintChatMessage()).thenReturn(true);

    // just return the input
    when(this.mockFilterService.censorNaughtyWords(anyString())).thenAnswer(args -> args.getArgument(0));

    return new McChatService(this.mockMinecraftProxyService, this.mockLogService, this.mockFilterService, this.mockSoundService, this.mockChatMateEventService, this.mockMessageService);
  }

  // The below methods should be used for mock data creation. It sets all
  // currently unused properties to null.
  //region mock data
  public static ChatItem createItem(Author msgAuthor, PartialChatMessage... messages) {
    return new ChatItem() {{
      author = msgAuthor;
      messageParts = messages;
    }};
  }

  public static Author createAuthor(String authorName) {
    return new Author() {{
      name = authorName;
      level = 0;
      levelProgress = 0.0;
    }};
  }

  public static PartialChatMessage createText(String messageText) {
    return new PartialChatMessage() {{
      type = PartialChatMessageType.text;
      text = messageText;
      isBold = false;
      isItalics = false;
    }};
  }

  public static PartialChatMessage createEmoji(String emojiName, String emojiLabel) {
    return new PartialChatMessage() {{
      type = PartialChatMessageType.emoji;
      name = emojiName;
      label = emojiLabel;
    }};
  }
  //endregion
}
