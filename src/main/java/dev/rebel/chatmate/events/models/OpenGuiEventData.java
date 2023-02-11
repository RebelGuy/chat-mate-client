package dev.rebel.chatmate.events.models;

import net.minecraft.client.gui.GuiScreen;

public class OpenGuiEventData {
  public final GuiScreen gui;

  public OpenGuiEventData(GuiScreen gui) {
    this.gui = gui;
  }
}
