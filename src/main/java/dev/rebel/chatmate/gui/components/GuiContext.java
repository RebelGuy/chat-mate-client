package dev.rebel.chatmate.gui.components;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.services.SoundService;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import net.minecraft.client.Minecraft;

public class GuiContext {
  public final Minecraft minecraft;
  public final FontEngine fontEngine;
  public final ForgeEventService forgeEventService;
  public final MouseEventService mouseEventService;
  public final SoundService soundService;

  public GuiContext(Minecraft minecraft, FontEngine fontEngine, ForgeEventService forgeEventService, MouseEventService mouseEventService, SoundService soundService) {
    this.minecraft = minecraft;
    this.fontEngine = fontEngine;
    this.forgeEventService = forgeEventService;
    this.mouseEventService = mouseEventService;
    this.soundService = soundService;
  }
}
