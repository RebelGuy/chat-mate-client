package dev.rebel.chatmate.services;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.gui.CustomGuiModList;
import dev.rebel.chatmate.gui.CustomGuiPause;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.OpenGui;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay;
import net.minecraft.client.gui.GuiScreen;

public class GuiService {
  private final ChatMate chatMate;
  private final Config config;
  private final ForgeEventService forgeEventService;

  public GuiService(ChatMate chatMate, Config config, ForgeEventService forgeEventService) {
    this.chatMate = chatMate;
    this.config = config;
    this.forgeEventService = forgeEventService;

    this.addEventHandlers();
  }

  private void addEventHandlers() {
    this.forgeEventService.onOpenGuiModList(this::onOpenGuiModList, null);
    this.forgeEventService.onOpenGuiIngameMenu(this::onOpenIngameMenu, null);
    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
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
}
