package dev.rebel.chatmate.gui.components;

import dev.rebel.chatmate.services.SoundService;
import dev.rebel.chatmate.services.events.ForgeEventService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiContext {
  public final Minecraft minecraft;
  public final FontRenderer fontRenderer;
  public final ForgeEventService forgeEventService;
  public final SoundService soundService;

  public GuiContext(Minecraft minecraft, FontRenderer fontRenderer, ForgeEventService forgeEventService, SoundService soundService) {
    this.minecraft = minecraft;
    this.fontRenderer = fontRenderer;
    this.forgeEventService = forgeEventService;
    this.soundService = soundService;
  }
}
