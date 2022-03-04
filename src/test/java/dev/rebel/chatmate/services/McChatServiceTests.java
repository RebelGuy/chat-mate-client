package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.models.publicObjects.chat.PublicMessageEmoji;
import dev.rebel.chatmate.models.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.models.publicObjects.chat.PublicMessageText;
import dev.rebel.chatmate.models.publicObjects.user.PublicChannelInfo;
import dev.rebel.chatmate.models.publicObjects.user.PublicLevelInfo;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.services.events.ChatMateEventService;
import net.minecraft.client.gui.FontRenderer;
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
  @Mock ImageService mockImageService;

  @Mock FontRenderer mockFontRenderer;

  PublicUser author1 = createAuthor("Author 1");
  PublicMessagePart text1 = createText("Text 1");
  PublicMessagePart text2 = createText("Text 2");
  PublicMessagePart textRebel = createText("Rebel");
  PublicMessagePart emoji1 = createEmoji("☺", ":smiling:");
  PublicMessagePart emoji2 = createEmoji("slightly smiling", ":slightly_smiling:");

  @Test
  public void addChat_ignoresIfCantPrintChat() {
    McChatService service = this.setupService();
    when(this.mockMinecraftProxyService.canPrintChatMessage()).thenReturn(false);

    service.printStreamChatItem(null);

    verify(this.mockMinecraftProxyService, never()).printChatMessage(anyString(), any());
  }

  @Test
  public void addChat_usesChatFilter() {
    PublicChatItem item = createItem(author1, text1);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    verify(this.mockFilterService).censorNaughtyWords(text1.textData.text);
  }

  @Test
  public void addChat_unicodeEmoji_PrintedDirectly() {
    PublicChatItem item = createItem(author1, emoji1);
    when(this.mockFontRenderer.getCharWidth(emoji1.emojiData.name.charAt(0))).thenReturn(1);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, emoji1.emojiData.name);
    verify(this.mockMinecraftProxyService).printChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_nonUnicodeEmoji_printLabel() {
    PublicChatItem item = createItem(author1, emoji2);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, emoji2.emojiData.label);
    verify(this.mockMinecraftProxyService).printChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_multipleText_joined() {
    PublicChatItem item = createItem(author1, text1, text2);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, text1.textData.text + text2.textData.text);
    verify(this.mockMinecraftProxyService).printChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_textEmoji_spacingBetween() {
    PublicChatItem item = createItem(author1, text1, emoji2, emoji1, text2);
    when(this.mockFontRenderer.getCharWidth(emoji1.emojiData.name.charAt(0))).thenReturn(1);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    String expected = getExpectedChatText(author1, text1.textData.text + " " + emoji2.emojiData.label + " " + emoji1.emojiData.name + " " + text2.textData.text);
    verify(this.mockMinecraftProxyService).printChatMessage(anyString(), ArgumentMatchers.argThat(cmp -> cmp.getUnformattedText().equals(expected)));
  }

  @Test
  public void addChat_chatMention_playsDing() {
    // so it *should* be possible to mock static methods using PowerMock by adding
    // @PrepareForTest(FilterService.class) to the method, and @RunWith(PowerMockRunner) to the class
    // but for some reason it causes everything JUnit related to break.
    // so for now just rely on the static method's implementation to work correctly (and it is tested in FilterServiceTests anyway).

    // note: if this test breaks it's probably because chat mentions are no longer hardcoded
    PublicChatItem item = createItem(author1, textRebel);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    verify(this.mockSoundService).playDing();
  }

  private static String getExpectedChatText(PublicUser author, String text) {
    return author.levelInfo.level + " VIEWER " + author.userInfo.channelName + " " + text;
  }

  private McChatService setupService() {
    when(this.mockMinecraftProxyService.getChatFontRenderer()).thenReturn(this.mockFontRenderer);
    when(this.mockMinecraftProxyService.canPrintChatMessage()).thenReturn(true);

    // just return the input
    when(this.mockFilterService.censorNaughtyWords(anyString())).thenAnswer(args -> args.getArgument(0));

    return new McChatService(this.mockMinecraftProxyService, this.mockLogService, this.mockFilterService, this.mockSoundService, this.mockChatMateEventService, this.mockMessageService, this.mockImageService);
  }

  // The below methods should be used for mock data creation. It sets all
  // currently unused properties to null.
  //region mock data
  public static PublicChatItem createItem(PublicUser msgAuthor, PublicMessagePart... messages) {
    return new PublicChatItem() {{
      author = msgAuthor;
      messageParts = messages;
    }};
  }

  public static PublicUser createAuthor(String authorName) {
    return new PublicUser() {{
      userInfo = new PublicChannelInfo() {{ channelName = authorName; }};
      levelInfo = new PublicLevelInfo() {{ level = 0; levelProgress = 0.0f; }};
    }};
  }

  public static PublicMessagePart createText(String messageText) {
    return new PublicMessagePart() {{
      type = MessagePartType.text;
      textData = new PublicMessageText() {{
        text = messageText;
        isBold = false;
        isItalics = false;
      }};
    }};
  }

  public static PublicMessagePart createEmoji(String emojiName, String emojiLabel) {
    return new PublicMessagePart() {{
      type = MessagePartType.emoji;
      emojiData = new PublicMessageEmoji() {{
        name = emojiName;
        label = emojiLabel;
      }};
    }};
  }
  //endregion
}
