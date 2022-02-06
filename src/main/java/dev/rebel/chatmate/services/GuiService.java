package dev.rebel.chatmate.services;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.gui.*;
import dev.rebel.chatmate.gui.dashboard.DashboardContext;
import dev.rebel.chatmate.gui.dashboard.DashboardScreen;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.KeyBindingService.ChatMateKeyEvent;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.models.OpenGui;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class GuiService {
  private final boolean isDev;
  private final Config config;
  private final ForgeEventService forgeEventService;
  private final MouseEventService mouseEventService;
  private final KeyBindingService keyBindingService;
  private final Minecraft minecraft;
  private final GuiChatMateHud guiChatMateHud;
  private final SoundService soundService;

  public GuiService(boolean isDev, Config config, ForgeEventService forgeEventService, MouseEventService mouseEventService, KeyBindingService keyBindingService, Minecraft minecraft, GuiChatMateHud guiChatMateHud, SoundService soundService) {
    this.isDev = isDev;
    this.config = config;
    this.forgeEventService = forgeEventService;
    this.mouseEventService = mouseEventService;
    this.keyBindingService = keyBindingService;
    this.minecraft = minecraft;
    this.guiChatMateHud = guiChatMateHud;
    this.soundService = soundService;

    this.addEventHandlers();
  }

  public void onDisplayDashboard() {
    this.minecraft.displayGuiScreen(new DashboardScreen(new DashboardContext(
        this.minecraft,
        this.minecraft.fontRendererObj,
        this.forgeEventService,
        this.mouseEventService,
        this.soundService
    )));
  }

  private void addEventHandlers() {
    this.forgeEventService.onOpenGuiModList(this::onOpenGuiModList, null);
    this.forgeEventService.onOpenGuiIngameMenu(this::onOpenIngameMenu, null);
    this.forgeEventService.onOpenChatSettingsMenu(this::onOpenChatSettingsMenu, null);
    this.forgeEventService.onOpenChat(this::onOpenChat, null);
    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
    this.forgeEventService.onRenderGameOverlay(this::onRenderGameOverlay, new RenderGameOverlay.Options(ElementType.ALL));

    this.keyBindingService.on(ChatMateKeyEvent.OPEN_CHAT_MATE_HUD, this::onOpenChatMateHud);
  }

  private OpenGui.Out onOpenGuiModList(OpenGui.In eventIn) {
    GuiScreen replaceWithGui = new CustomGuiModList(null, this.minecraft, this.config);
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

    GuiScreen replaceWithGui = new CustomGuiChat(this.config, defaultValue);
    return new OpenGui.Out(replaceWithGui);
  }

  /** Moves up the chat a bit so that it doesn't cover the bottom GUI. */
  private RenderChatGameOverlay.Out onRenderChatGameOverlay(RenderChatGameOverlay.In eventIn) {
    int newPosY = eventIn.posY - this.config.getChatVerticalDisplacement().get();
    return new RenderChatGameOverlay.Out(eventIn.posX, newPosY);
  }

  private RenderGameOverlay.Out onRenderGameOverlay(RenderGameOverlay.In in) {
    if (this.config.getApiEnabled().get() && this.config.getHudEnabled().get()) {
      this.guiChatMateHud.renderGameOverlay();
    }

    return new RenderGameOverlay.Out();
  }

  private Boolean onOpenChatMateHud() {
    if (this.config.getHudEnabled().get()) {
      // key events don't fire when we are in a menu, so don't need to worry about closing this GUI when the key is pressed again
      GuiChatMateHudScreen hudScreen = new GuiChatMateHudScreen(this.minecraft, this.mouseEventService, this.guiChatMateHud);
      this.minecraft.displayGuiScreen(hudScreen);
      return true;
    } else {
      return false;
    }
  }
}
