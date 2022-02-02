package dev.rebel.chatmate.gui.dashboard;

import dev.rebel.chatmate.gui.components.GuiContext;
import dev.rebel.chatmate.services.SoundService;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class DashboardContext extends GuiContext {
  public DashboardContext(Minecraft minecraft, FontRenderer fontRenderer, ForgeEventService forgeEventService, MouseEventService mouseEventService, SoundService soundService) {
    super(minecraft, fontRenderer, forgeEventService, mouseEventService, soundService);
  }
}
