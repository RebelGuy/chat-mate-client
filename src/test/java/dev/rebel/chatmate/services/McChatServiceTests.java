package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Environment;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;
import dev.rebel.chatmate.config.Config.CommandMessageChatVisibility;
import dev.rebel.chatmate.events.*;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.gui.ChatComponentRenderer;
import dev.rebel.chatmate.gui.CustomGuiNewChat;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHudStore;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ScreenRenderer;
import dev.rebel.chatmate.gui.chat.ContainerChatComponent;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.StatefulEmitter;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessageEmoji;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessageText;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicLevelInfo;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.stores.CommandApiStore;
import dev.rebel.chatmate.stores.DonationApiStore;
import dev.rebel.chatmate.stores.LivestreamApiStore;
import dev.rebel.chatmate.stores.RankApiStore;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class) // this lets us use @Mock for auto-initialising mock objects
public class McChatServiceTests {
  @Mock DimFactory mockDimFactory;
  @Mock CustomGuiNewChat mockCustomGuiNewChat;
  @Mock MinecraftProxyService mockMinecraftProxyService;
  @Mock LogService mockLogService;
  @Mock FilterService mockFilterService;
  @Mock SoundService mockSoundService;
  @Mock ChatMateEventService mockChatMateEventService;
  @Mock MessageService mockMessageService;
  @Mock ImageService mockImageService;
  @Mock ChatMateChatService mockChatMateService;
  @Mock FontEngine mockFontEngine;
  @Mock MinecraftChatEventService mockMinecraftChatEventService;

  @Mock Config mockConfig;
  @Mock StatefulEmitter<Boolean> mockShowChatPlatformIconEmitter;
  @Mock StatefulEmitter<Boolean> mockDebugModeEnabledEmitter;
  @Mock StatefulEmitter<CommandMessageChatVisibility> mockCommandMessageChatVisibilityEmitter;

  PublicUser author1 = createAuthor("Author 1");
  PublicMessagePart text1 = createText("Text 1");
  PublicMessagePart text2 = createText("Text 2");
  PublicMessagePart textRebel = createText("Rebel");
  PublicMessagePart emoji1 = createEmoji("☺", ":smiling:");
  PublicMessagePart emoji2 = createEmoji("slightly smiling", ":slightly_smiling:");

  @Before
  public void setup() {
    // assume nonempty, just return the input component
    when(this.mockMessageService.ensureNonempty(ArgumentMatchers.any(), anyString())).thenAnswer(i -> i.getArgument(0));

    when(this.mockMessageService.getUserComponent(ArgumentMatchers.any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean())).thenAnswer(i -> {
      PublicUser user = i.getArgument(0);
      return new ContainerChatComponent(new ChatComponentText(user.channel.displayName), user);
    });

    // this is for the ViewerTagComponent
    when(this.mockConfig.getShowChatPlatformIconEmitter()).thenReturn(this.mockShowChatPlatformIconEmitter);

    when(this.mockConfig.getCommandMessageChatVisibilityEmitter()).thenReturn(this.mockCommandMessageChatVisibilityEmitter);
    when(this.mockMessageService.getRankComponent(any())).thenReturn(new ChatComponentText("VIEWER"));
  }

  @Test
  public void addChat_ignoresIfUserHasActivePunishments() {
    author1.activeRanks = new PublicUserRank[] {
        new PublicUserRank() {{
          rank = new PublicRank() {{
            name = RankName.BAN;
            group = RankGroup.PUNISHMENT;
          }};
        }}
    };
    PublicChatItem item = createItem(author1, text1);
    McChatService service = this.setupService();

    service.printStreamChatItem(item);

    verify(this.mockMinecraftProxyService, never()).printChatMessage(anyString(), any());
  }

  @Test
  public void addChat_printsMessageIfIsCommandAndCommandsAreShown() {
    PublicChatItem item = createItem(author1, text1);
    item.commandId = 1;
    McChatService service = this.setupService();
    when(this.mockCommandMessageChatVisibilityEmitter.get()).thenReturn(CommandMessageChatVisibility.SHOWN);

    service.printStreamChatItem(item);

    verify(this.mockMinecraftProxyService).printChatMessage(anyString(), any());
  }

  @Test
  public void addChat_ignoresIfMessageIsCommandAndCommandsAreHidden() {
    PublicChatItem item = createItem(author1, text1);
    item.commandId = 1;
    McChatService service = this.setupService();
    when(this.mockCommandMessageChatVisibilityEmitter.get()).thenReturn(CommandMessageChatVisibility.HIDDEN);

    service.printStreamChatItem(item);

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
    when(this.mockFontEngine.getCharWidth(emoji1.emojiData.name.charAt(0))).thenReturn(new Dim(() -> 1, DimAnchor.GUI).setGui(1));
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
    when(this.mockFontEngine.getCharWidth(emoji1.emojiData.name.charAt(0))).thenReturn(new Dim(() -> 1, DimAnchor.GUI).setGui(1));
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

  @Test
  public void identifyPlatformChanged_refreshesChat() {
    // this is a weirdly structured test thanks to java.
    // upon instantiation, the service will subscribe to the mock emitter's onChange function
    McChatService service = this.setupService();

    // since we are dealing with a void method, the only way to retrieve the input is
    // using an ArgumentCaptor and verify()
    ArgumentCaptor<EventCallback<Boolean>> captor = ArgumentCaptor.forClass(EventCallback.class);
    verify(this.mockShowChatPlatformIconEmitter).onChange(captor.capture());

    // notify the subscriber that the value has changed to true
    captor.getValue().dispatch(new Event<>(true));

    // this should have triggered a chat refresh
    verify(this.mockMinecraftProxyService).refreshChat();
  }

  private static String getExpectedChatText(PublicUser author, String text) {
    return author.levelInfo.level + " VIEWER " + author.channel.displayName + " " + text;
  }

  private McChatService setupService() {
    // just return the input
    when(this.mockFilterService.censorNaughtyWords(anyString())).thenAnswer(args -> args.getArgument(0));

    when(this.mockConfig.getDebugModeEnabledEmitter()).thenReturn(this.mockDebugModeEnabledEmitter);

    return new McChatService(this.mockMinecraftProxyService,
        this.mockLogService,
        this.mockFilterService,
        this.mockSoundService,
        this.mockChatMateEventService,
        this.mockMessageService,
        this.mockImageService,
        this.mockConfig,
        this.mockChatMateService,
        this.mockFontEngine,
        this.mockDimFactory,
        this.mockCustomGuiNewChat,
        this.mockMinecraftChatEventService,
        new InteractiveContext(
            Mockito.mock(ScreenRenderer.class),
            Mockito.mock(MouseEventService.class),
            Mockito.mock(KeyboardEventService.class),
            Mockito.mock(DimFactory.class),
            Mockito.mock(Minecraft.class),
            Mockito.mock(FontEngine.class),
            Mockito.mock(ClipboardService.class),
            Mockito.mock(SoundService.class),
            Mockito.mock(CursorService.class),
            Mockito.mock(MinecraftProxyService.class),
            Mockito.mock(UrlService.class),
            Mockito.mock(Environment.class),
            Mockito.mock(LogService.class),
            Mockito.mock(MinecraftChatService.class),
            Mockito.mock(ForgeEventService.class),
            Mockito.mock(ChatComponentRenderer.class),
            Mockito.mock(RankApiStore.class),
            Mockito.mock(LivestreamApiStore.class),
            Mockito.mock(DonationApiStore.class),
            Mockito.mock(CommandApiStore.class),
            this.mockConfig,
            Mockito.mock(ImageService.class),
            Mockito.mock(DonationHudStore.class)
        ));
  }

  // The below methods should be used for mock data creation. It sets all
  // currently unused properties to null.
  //region mock data
  public static PublicChatItem createItem(PublicUser msgAuthor, PublicMessagePart... messages) {
    return new PublicChatItem() {{
      author = msgAuthor;
      messageParts = messages;
      platform = ChatPlatform.Youtube;
      commandId = null;
    }};
  }

  public static PublicUser createAuthor(String authorName) {
    return new PublicUser() {{
      channel = new PublicChannel() {{ displayName = authorName; }};
      levelInfo = new PublicLevelInfo() {{ level = 0; levelProgress = 0.0f; }};
      activeRanks = new PublicUserRank[0];
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
