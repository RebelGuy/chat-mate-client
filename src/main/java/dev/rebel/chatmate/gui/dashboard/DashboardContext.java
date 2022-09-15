package dev.rebel.chatmate.gui.dashboard;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.components.GuiContext;
import dev.rebel.chatmate.services.SoundService;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import net.minecraft.client.Minecraft;

public class DashboardContext extends GuiContext {
  public DashboardContext(Minecraft minecraft, FontEngine fontEngine, ForgeEventService forgeEventService, MouseEventService mouseEventService, SoundService soundService) {
    super(minecraft, fontEngine, forgeEventService, mouseEventService, soundService);
  }
}
