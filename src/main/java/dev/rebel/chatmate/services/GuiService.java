package dev.rebel.chatmate.services;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.gui.CustomGuiModList;
import dev.rebel.chatmate.gui.CustomGuiPause;
import dev.rebel.chatmate.gui.GuiChatMateHud;
import dev.rebel.chatmate.gui.GuiChatMateHudScreen;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.KeyBindingService.ChatMateKeyEvent;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.OpenGui;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

public class GuiService {
  private final ChatMate chatMate;
  private final Config config;
  private final ForgeEventService forgeEventService;
  private final KeyBindingService keyBindingService;
  private final Minecraft minecraft;
  private final GuiChatMateHud guiChatMateHud;

  public GuiService(ChatMate chatMate, Config config, ForgeEventService forgeEventService, KeyBindingService keyBindingService, Minecraft minecraft, GuiChatMateHud guiChatMateHud) {
    this.chatMate = chatMate;
    this.config = config;
    this.forgeEventService = forgeEventService;
    this.keyBindingService = keyBindingService;
    this.minecraft = minecraft;
    this.guiChatMateHud = guiChatMateHud;

    this.addEventHandlers();
  }

  private void addEventHandlers() {
    this.forgeEventService.onOpenGuiModList(this::onOpenGuiModList, null);
    this.forgeEventService.onOpenGuiIngameMenu(this::onOpenIngameMenu, null);
    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
    this.forgeEventService.onRenderGameOverlay(this::onRenderGameOverlay, new RenderGameOverlay.Options(ElementType.ALL));

    this.keyBindingService.on(ChatMateKeyEvent.OPEN_CHAT_MATE_HUD, this::onOpenChatMateHud);
  }

  private OpenGui.Out onOpenGuiModList(OpenGui.In eventIn) {
    GuiScreen replaceWithGui = new CustomGuiModList(null, this.chatMate);
    return new OpenGui.Out(replaceWithGui);
  }

  private OpenGui.Out onOpenIngameMenu(OpenGui.In eventIn) {
    GuiScreen replaceWithGui = new CustomGuiPause(this.config);
    return new OpenGui.Out(replaceWithGui);
  }

  /** Moves up the chat a bit so that it doesn't cover the bottom GUI. */
  private RenderChatGameOverlay.Out onRenderChatGameOverlay(RenderChatGameOverlay.In eventIn) {
    int newPosY = eventIn.posY - this.config.chatVerticalDisplacement.get();
    return new RenderChatGameOverlay.Out(eventIn.posX, newPosY);
  }

  private RenderGameOverlay.Out onRenderGameOverlay(RenderGameOverlay.In in) {
    if (this.config.hudEnabled.get()) {
      this.guiChatMateHud.renderGameOverlay();
    }

    return new RenderGameOverlay.Out();
  }

  private Boolean onOpenChatMateHud() {
    if (this.config.hudEnabled.get()) {
      // key events don't fire when we are in a menu, so don't need to worry about closing this GUI when the key is pressed again
      GuiChatMateHudScreen hudScreen = new GuiChatMateHudScreen(this.minecraft, this.guiChatMateHud, this.forgeEventService);
      this.minecraft.displayGuiScreen(hudScreen);
      return true;
    } else {
      return false;
    }
  }
}