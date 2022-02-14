package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.RenderChatGameOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class CustomGuiNewChat extends GuiNewChat {
  Minecraft minecraft;
  ForgeEventService forgeEventService;

  public CustomGuiNewChat(Minecraft minecraft, ForgeEventService forgeEventService) {
    super(minecraft);
    this.minecraft = minecraft;
    this.forgeEventService = forgeEventService;

    this.forgeEventService.onRenderChatGameOverlay(this::onRenderChatGameOverlay, null);
  }

  public RenderChatGameOverlay.Out onRenderChatGameOverlay(RenderChatGameOverlay.In eventIn) {
    RenderGameOverlayEvent.Chat event = eventIn.event;
    event.setCanceled(true);

    // copied from the GuiIngameForge::renderChat, except using our own GuiNewChat implementation
    GlStateManager.pushMatrix();
    GlStateManager.translate((float)event.posX, (float)event.posY, 0.0F);
    super.drawChat(this.minecraft.ingameGUI.getUpdateCounter());
    GlStateManager.popMatrix();
    this.minecraft.mcProfiler.endSection();

    return new RenderChatGameOverlay.Out();
  }
}
