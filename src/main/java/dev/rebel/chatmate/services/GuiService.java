package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Environment;
import dev.rebel.chatmate.api.ChatMateWebsocketClient;
import dev.rebel.chatmate.api.proxy.AccountEndpointProxy;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.gui.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudService;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHudStore;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ScreenRenderer;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudScreen;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.api.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.api.proxy.UserEndpointProxy;
import dev.rebel.chatmate.services.KeyBindingService.ChatMateKeyEvent;
import dev.rebel.chatmate.events.ForgeEventService;
import dev.rebel.chatmate.events.KeyboardEventService;
import dev.rebel.chatmate.events.MouseEventService;
import dev.rebel.chatmate.events.models.OpenGuiEventData;
import dev.rebel.chatmate.stores.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class GuiService {
  private final boolean isDev;
  private final LogService logService;
  private final Config config;
  private final ForgeEventService forgeEventService;
  private final MouseEventService mouseEventService;
  private final KeyBindingService keyBindingService;
  private final Minecraft minecraft;
  private final MinecraftProxyService minecraftProxyService;
  private final SoundService soundService;
  private final DimFactory dimFactory;
  private final ContextMenuStore contextMenuStore;
  private final ContextMenuService contextMenuService;
  private final CursorService cursorService;
  private final KeyboardEventService keyboardEventService;
  private final ClipboardService clipboardService;
  private final UrlService urlService;
  private final StreamerEndpointProxy streamerEndpointProxy;
  private final Environment environment;
  private final MinecraftChatService minecraftChatService;
  private final CustomGuiIngame customGuiIngame;
  private final FontEngine fontEngine;
  private final FontEngineProxy fontEngineProxy;
  private final DonationEndpointProxy donationEndpointProxy;
  private final ChatMateHudScreen chatMateHudScreen;
  private final ChatComponentRenderer chatComponentRenderer;
  private final StatusService statusService;
  private final ApiRequestService apiRequestService;
  private final UserEndpointProxy userEndpointProxy;
  private final MessageService messageService;
  private final LivestreamApiStore livestreamApiStore;
  private final DonationApiStore donationApiStore;
  private final RankApiStore rankApiStore;
  private final CommandApiStore commandApiStore;
  private final StreamerApiStore streamerApiStore;
  private final CustomGuiNewChat customGuiNewChat;
  private final ImageService imageService;
  private final DonationHudStore donationHudStore;
  private final ChatMateHudService chatMateHudService;
  private final AccountEndpointProxy accountEndpointProxy;
  private final String dataFolder;
  private final ChatMateWebsocketClient chatMateWebsocketClient;

  public GuiService(boolean isDev,
                    LogService logService,
                    Config config,
                    ForgeEventService forgeEventService,
                    MouseEventService mouseEventService,
                    KeyBindingService keyBindingService,
                    Minecraft minecraft,
                    MinecraftProxyService minecraftProxyService,
                    SoundService soundService,
                    DimFactory dimFactory,
                    ContextMenuStore contextMenuStore,
                    ContextMenuService contextMenuService,
                    CursorService cursorService,
                    KeyboardEventService keyboardEventService,
                    ClipboardService clipboardService,
                    UrlService urlService,
                    StreamerEndpointProxy streamerEndpointProxy,
                    Environment environment,
                    MinecraftChatService minecraftChatService,
                    CustomGuiIngame customGuiIngame,
                    FontEngine fontEngine,
                    FontEngineProxy fontEngineProxy,
                    DonationEndpointProxy donationEndpointProxy,
                    ChatMateHudScreen chatMateHudScreen,
                    ChatComponentRenderer chatComponentRenderer,
                    StatusService statusService,
                    ApiRequestService apiRequestService,
                    UserEndpointProxy userEndpointProxy,
                    MessageService messageService,
                    LivestreamApiStore livestreamApiStore,
                    DonationApiStore donationApiStore,
                    RankApiStore rankApiStore,
                    CommandApiStore commandApiStore,
                    StreamerApiStore streamerApiStore,
                    CustomGuiNewChat customGuiNewChat,
                    ImageService imageService,
                    DonationHudStore donationHudStore,
                    ChatMateHudService chatMateHudService,
                    AccountEndpointProxy accountEndpointProxy,
                    String dataFolder,
                    ChatMateWebsocketClient chatMateWebsocketClient) {
    this.isDev = isDev;
    this.logService = logService;
    this.config = config;
    this.forgeEventService = forgeEventService;
    this.mouseEventService = mouseEventService;
    this.keyBindingService = keyBindingService;
    this.minecraft = minecraft;
    this.minecraftProxyService = minecraftProxyService;
    this.soundService = soundService;
    this.dimFactory = dimFactory;
    this.contextMenuStore = contextMenuStore;
    this.contextMenuService = contextMenuService;
    this.cursorService = cursorService;
    this.keyboardEventService = keyboardEventService;
    this.clipboardService = clipboardService;
    this.urlService = urlService;
    this.streamerEndpointProxy = streamerEndpointProxy;
    this.environment = environment;
    this.minecraftChatService = minecraftChatService;
    this.customGuiIngame = customGuiIngame;
    this.fontEngine = fontEngine;
    this.fontEngineProxy = fontEngineProxy;
    this.donationEndpointProxy = donationEndpointProxy;
    this.chatMateHudScreen = chatMateHudScreen;
    this.chatComponentRenderer = chatComponentRenderer;
    this.statusService = statusService;
    this.apiRequestService = apiRequestService;
    this.userEndpointProxy = userEndpointProxy;
    this.messageService = messageService;
    this.livestreamApiStore = livestreamApiStore;
    this.donationApiStore = donationApiStore;
    this.rankApiStore = rankApiStore;
    this.commandApiStore = commandApiStore;
    this.streamerApiStore = streamerApiStore;
    this.customGuiNewChat = customGuiNewChat;
    this.imageService = imageService;
    this.donationHudStore = donationHudStore;
    this.chatMateHudService = chatMateHudService;
    this.accountEndpointProxy = accountEndpointProxy;
    this.dataFolder = dataFolder;
    this.chatMateWebsocketClient = chatMateWebsocketClient;

    this.addEventHandlers();
  }

  public void displayDashboard() {
    this.displayDashboard(null);
  }

  public void displayDashboard(@Nullable DashboardRoute route) {
    this.minecraft.displayGuiScreen(this.createDashboardScreen(route));
  }

  private InteractiveScreen createDashboardScreen(@Nullable DashboardRoute route) {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen, InteractiveScreenType.DASHBOARD);
    screen.setMainElement(new ChatMateDashboardElement(
        context,
        screen,
        route,
        this.streamerEndpointProxy,
        this.statusService,
        this.apiRequestService,
        this.userEndpointProxy,
        this.messageService,
        this.config,
        this.chatMateHudService,
        this.accountEndpointProxy,
        this.dataFolder,
        this.donationEndpointProxy,
        this.chatMateWebsocketClient)
    );
    return screen;
  }

  private void addEventHandlers() {
    this.forgeEventService.onOpenGuiModList(this::onOpenGuiModList);
    this.forgeEventService.onOpenGuiIngameMenu(this::onOpenIngameMenu);
    this.forgeEventService.onOpenChatSettingsMenu(this::onOpenChatSettingsMenu);
    this.forgeEventService.onOpenChat(this::onOpenChat);
    this.forgeEventService.onClientTick(this::onClientTick);

    this.keyBindingService.on(ChatMateKeyEvent.OPEN_CHAT_MATE_HUD, this::onOpenChatMateHud);
  }

  private void onClientTick(Event<?> event) {
    if (this.minecraft.ingameGUI != customGuiIngame) {
      if (customGuiIngame == null) {
        throw new RuntimeException("The CustomGuiIngame object has not been instantiated.");
      }

      // Replace the ingameGui with our own implementation as soon as the title screen is shown.
      // The reason we can't do this before is because, in Minecraft::start literally two lines after
      // everything is initialised and this event is fired, the ingameGUI is initialised by Forge, which would just
      // overwrite the value we just set. Instead, wait until the game has been fully initialised.
      this.minecraft.ingameGUI = customGuiIngame;
      this.logService.logInfo(this, "Setting minecraft.ingameGUI to our custom IngameGui implementation.");
    }
  }

  private void onOpenGuiModList(Event<OpenGuiEventData> event) {
    // : - <|
    GuiScreen replaceWithGui = new CustomGuiModList(null, this.minecraft, this.config, this, this.fontEngineProxy);
    event.modifyData(new OpenGuiEventData(replaceWithGui));
  }

  private void onOpenIngameMenu(Event<OpenGuiEventData> event) {
    GuiScreen replaceWithGui = new CustomGuiPause(this, this.config);
    event.modifyData(new OpenGuiEventData(replaceWithGui));
  }

  private void onOpenChatSettingsMenu(Event<OpenGuiEventData> event) {
    GuiScreen replaceWithGui = new CustomScreenChatOptions(null, this.minecraft.gameSettings, this.config);
    event.modifyData(new OpenGuiEventData(replaceWithGui));
  }

  private void onOpenChat(Event<OpenGuiEventData> event) {
    GuiChat guiChat = (GuiChat)event.getData().gui;

    // HACK: we need to get this private field, otherwise pressing "/" no longer pre-fills the chat window
    String defaultValue = "";
      // obfuscated field name found using trial and error
      String fieldName = this.isDev ? "defaultInputFieldText" : "field_146409_v";
    try {
      Field field = GuiChat.class.getDeclaredField(fieldName);
      field.setAccessible(true); // otherwise we get an IllegalAccessException
      defaultValue = (String)field.get(guiChat);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Unable to override field value GuiChat.%s. `isDev` is %s. Error message: %s", fieldName, this.isDev, e.getMessage()));
    }

    GuiScreen replaceWithGui = new CustomGuiChat(
        defaultValue,
        this.minecraftProxyService,
        this.mouseEventService,
        this.contextMenuStore,
        this.contextMenuService,
        this.cursorService,
        this.urlService,
        this.forgeEventService,
        this.customGuiNewChat);
    event.modifyData(new OpenGuiEventData(replaceWithGui));
  }

  private Boolean onOpenChatMateHud() {
    // this intentionally still opens even if the HUD is disabled, because how else can we display the context menu?
    if (this.config.getChatMateEnabledEmitter().get()) {
      this.minecraft.displayGuiScreen(this.chatMateHudScreen);
      return true;
    } else {
      return false;
    }
  }

  private InteractiveScreen.InteractiveContext createInteractiveContext() {
    return new InteractiveScreen.InteractiveContext(new ScreenRenderer(),
        this.mouseEventService,
        this.keyboardEventService,
        this.dimFactory,
        this.minecraft,
        this.fontEngine,
        this.clipboardService,
        this.soundService,
        this.cursorService,
        this.minecraftProxyService,
        this.urlService,
        this.environment,
        this.logService,
        this.minecraftChatService,
        this.forgeEventService,
        this.chatComponentRenderer,
        this.rankApiStore,
        this.livestreamApiStore,
        this.donationApiStore,
        this.commandApiStore,
        this.streamerApiStore,
        this.config,
        this.imageService,
        this.donationHudStore,
        this.statusService);
  }
}
