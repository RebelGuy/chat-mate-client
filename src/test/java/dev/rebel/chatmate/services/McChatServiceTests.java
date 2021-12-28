package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.GetChatResponse.Author;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.models.chat.GetChatResponse.PartialChatMessage;
import dev.rebel.chatmate.models.chat.PartialChatMessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class) // this lets us use @Mock for auto-initialising mock objects
public class McChatServiceTests {
  @Mock Minecraft mockMinecraft;
  @Mock LoggingService mockLoggingService;
  @Mock FilterService mockFilterService;
  @Mock SoundService mockSoundService;

  @Mock GuiIngame mockGuiIngame;
  @Mock FontRenderer mockFontRenderer;
  @Mock GuiNewChat mockChatGui;
  ArgumentCaptor<GuiNewChat> chatGuiCaptor = ArgumentCaptor.forClass(GuiNewChat.class);

  Author author1 = new Author() {{ name = "Author 1"; }};
  PartialChatMessage text1 = createText("Text 1");
  PartialChatMessage text2 = createText("Text 2");
  PartialChatMessage emoji1 = createEmoji("☺", ":smiling:");
  PartialChatMessage emoji2 = createEmoji("slightly smiling", ":slightly_smiling:");

  @Test
  public void addChat_handlesNullGui() {
    this.mockMinecraft.ingameGUI = null;
    McChatService service = this.setupService();

    service.addToMcChat(null);
  }

  @Test
  public void addChat_usesChatFilter() {
    ChatItem item = createItem(author1, text1);
    McChatService service = this.setupService();

    service.addToMcChat(item);

    verify(this.mockFilterService).censorNaughtyWords(text1.text);
  }

  @Test
  public void addChat_unicodeEmoji_PrintedDirectly() {
    ChatItem item = createItem(author1, emoji1);
    when(this.mockFontRenderer.getCharWidth(emoji1.name.charAt(0))).thenReturn(1);
    McChatService service = this.setupService();

    service.addToMcChat(item);

    String expected = getExpectedChatText(author1, emoji1.name);
    verify(this.mockChatGui).printChatMessage(ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_nonUnicodeEmoji_printLabel() {
    ChatItem item = createItem(author1, emoji2);
    McChatService service = this.setupService();

    service.addToMcChat(item);

    String expected = getExpectedChatText(author1, emoji2.label);
    verify(this.mockChatGui).printChatMessage(ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_multipleText_joined() {
    ChatItem item = createItem(author1, text1, text2);
    McChatService service = this.setupService();

    service.addToMcChat(item);

    String expected = getExpectedChatText(author1, text1.text + text2.text);
    verify(this.mockChatGui).printChatMessage(ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_textEmoji_spacingBetween() {
    ChatItem item = createItem(author1, text1, emoji2, emoji1, text2);
    when(this.mockFontRenderer.getCharWidth(emoji1.name.charAt(0))).thenReturn(1);
    McChatService service = this.setupService();

    service.addToMcChat(item);

    String expected = getExpectedChatText(author1, text1.text + " " + emoji2.label + " " + emoji1.name + " " + text2.text);
    verify(this.mockChatGui).printChatMessage(ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  private static String getExpectedChatText(Author author, String text) {
    return "VIEWER " + author.name + " " + text;
  }

  private McChatService setupService() {
    this.mockMinecraft.ingameGUI = this.mockGuiIngame;
    when(this.mockGuiIngame.getFontRenderer()).thenReturn(this.mockFontRenderer);
    when(this.mockGuiIngame.getChatGUI()).thenReturn(this.mockChatGui);

    // just return the input
    when(this.mockFilterService.censorNaughtyWords(anyString())).thenAnswer(args -> args.getArguments()[0]);

    return new McChatService(this.mockMinecraft, this.mockLoggingService, this.mockFilterService, this.mockSoundService);
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
