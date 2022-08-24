package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Environment;
import dev.rebel.chatmate.gui.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.ChatMateDashboardElement;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.DashboardRoute;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ScreenRenderer;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.services.KeyBindingService.ChatMateKeyEvent;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.KeyboardEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.models.OpenGui;
import dev.rebel.chatmate.services.events.models.Tick;
import dev.rebel.chatmate.services.events.models.Tick.In;
import dev.rebel.chatmate.services.events.models.Tick.Out;
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
  private final GuiChatMateHud guiChatMateHud;
  private final SoundService soundService;
  private final DimFactory dimFactory;
  private final ContextMenuStore contextMenuStore;
  private final ContextMenuService contextMenuService;
  private final CursorService cursorService;
  private final KeyboardEventService keyboardEventService;
  private final ClipboardService clipboardService;
  private final UrlService urlService;
  private final ChatMateEndpointProxy chatMateEndpointProxy;
  private final Environment environment;
  private final MinecraftChatService minecraftChatService;
  private final CustomGuiIngame customGuiIngame;
  private final FontEngine fontEngine;
  private final FontEngineProxy fontEngineProxy;
  private final DonationEndpointProxy donationEndpointProxy;

  public GuiService(boolean isDev,
                    LogService logService,
                    Config config,
                    ForgeEventService forgeEventService,
                    MouseEventService mouseEventService,
                    KeyBindingService keyBindingService,
                    Minecraft minecraft,
                    MinecraftProxyService minecraftProxyService,
                    GuiChatMateHud guiChatMateHud,
                    SoundService soundService,
                    DimFactory dimFactory,
                    ContextMenuStore contextMenuStore,
                    ContextMenuService contextMenuService,
                    CursorService cursorService,
                    KeyboardEventService keyboardEventService,
                    ClipboardService clipboardService,
                    UrlService urlService,
                    ChatMateEndpointProxy chatMateEndpointProxy,
                    Environment environment,
                    MinecraftChatService minecraftChatService,
                    CustomGuiIngame customGuiIngame,
                    FontEngine fontEngine,
                    FontEngineProxy fontEngineProxy,
                    DonationEndpointProxy donationEndpointProxy) {
    this.isDev = isDev;
    this.logService = logService;
    this.config = config;
    this.forgeEventService = forgeEventService;
    this.mouseEventService = mouseEventService;
    this.keyBindingService = keyBindingService;
    this.minecraft = minecraft;
    this.minecraftProxyService = minecraftProxyService;
    this.guiChatMateHud = guiChatMateHud;
    this.soundService = soundService;
    this.dimFactory = dimFactory;
    this.contextMenuStore = contextMenuStore;
    this.contextMenuService = contextMenuService;
    this.cursorService = cursorService;
    this.keyboardEventService = keyboardEventService;
    this.clipboardService = clipboardService;
    this.urlService = urlService;
    this.chatMateEndpointProxy = chatMateEndpointProxy;
    this.environment = environment;
    this.minecraftChatService = minecraftChatService;
    this.customGuiIngame = customGuiIngame;
    this.fontEngine = fontEngine;
    this.fontEngineProxy = fontEngineProxy;
    this.donationEndpointProxy = donationEndpointProxy;

    this.addEventHandlers();
  }

  public void displayDashboard() {
    this.displayDashboard(null);
  }

  public void displayDashboard(@Nullable DashboardRoute route) {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen);
    screen.setMainElement(new ChatMateDashboardElement(context, screen, route, this.chatMateEndpointProxy, this.donationEndpointProxy));
    this.minecraft.displayGuiScreen(screen);
  }

  private void addEventHandlers() {
    this.forgeEventService.onOpenGuiModList(this::onOpenGuiModList, null);
    this.forgeEventService.onOpenGuiIngameMenu(this::onOpenIngameMenu, null);
    this.forgeEventService.onOpenChatSettingsMenu(this::onOpenChatSettingsMenu, null);
    this.forgeEventService.onOpenChat(this::onOpenChat, null);
    this.forgeEventService.onRenderTick(this::onRender, null);
    this.forgeEventService.onClientTick(this::onClientTick, null);

    this.keyBindingService.on(ChatMateKeyEvent.OPEN_CHAT_MATE_HUD, this::onOpenChatMateHud);
  }

  private Out onClientTick(In in) {
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
    return new Tick.Out();
  }

  private OpenGui.Out onOpenGuiModList(OpenGui.In eventIn) {
    // : - <|
    GuiScreen replaceWithGui = new CustomGuiModList(null, this.minecraft, this.config, this, this.fontEngineProxy);
    return new OpenGui.Out(replaceWithGui);
  }

  private OpenGui.Out onOpenIngameMenu(OpenGui.In eventIn) {
    GuiScreen replaceWithGui = new CustomGuiPause(this, this.config);
    return new OpenGui.Out(replaceWithGui);
  }

  private OpenGui.Out onOpenChatSettingsMenu(OpenGui.In eventIn) {
    GuiScreen replaceWithGui = new CustomScreenChatOptions(null, this.minecraft.gameSettings, this.config);
    return new OpenGui.Out(replaceWithGui);
  }

  private OpenGui.Out onOpenChat(OpenGui.In eventIn) {
    GuiChat guiChat = (GuiChat)eventIn.gui;

    // HACK: we need to get this private field, otherwise pressing "/" no longer pre-fills the chat window
    String defaultValue = "";
    try {
      // obfuscated field name found using trial and error
      String fieldName = this.isDev ? "defaultInputFieldText" : "field_146409_v";
      Field field = GuiChat.class.getDeclaredField(fieldName);
      field.setAccessible(true); // otherwise we get an IllegalAccessException
      defaultValue = (String)field.get(guiChat);
    } catch (Exception e) { throw new RuntimeException("This should never happen"); }

    GuiScreen replaceWithGui = new CustomGuiChat(
        defaultValue,
        this.minecraftProxyService,
        this.mouseEventService,
        this.contextMenuStore,
        this.contextMenuService,
        this.cursorService,
        this.urlService,
        this.forgeEventService);
    return new OpenGui.Out(replaceWithGui);
  }

  private Out onRender(In in) {
    if (this.config.getChatMateEnabledEmitter().get() && this.config.getHudEnabledEmitter().get() && !this.minecraft.gameSettings.showDebugInfo) {
      if (this.minecraft.currentScreen instanceof GuiChatMateHudScreen) {
        ((GuiChatMateHudScreen)this.minecraft.currentScreen).renderGameOverlayPreHud();
      }
      this.guiChatMateHud.renderGameOverlay();
      if (this.minecraft.currentScreen instanceof GuiChatMateHudScreen) {
        ((GuiChatMateHudScreen)this.minecraft.currentScreen).renderGameOverlayPostHud();
      }
    }

    return new Out();
  }

  private Boolean onOpenChatMateHud() {
    if (this.config.getHudEnabledEmitter().get()) {
      // key events don't fire when we are in a menu, so don't need to worry about closing this GUI when the key is pressed again
      GuiChatMateHudScreen hudScreen = new GuiChatMateHudScreen(this.minecraft, this.mouseEventService, this.dimFactory, this.guiChatMateHud, this.contextMenuService);
      this.minecraft.displayGuiScreen(hudScreen);
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
        this.minecraftChatService);
  }
}
