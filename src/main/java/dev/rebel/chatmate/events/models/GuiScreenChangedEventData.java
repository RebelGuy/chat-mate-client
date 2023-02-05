package dev.rebel.chatmate.events.models;

import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

public class GuiScreenChangedEventData {
  private final GuiScreen fromScreen;
  private final GuiScreen toScreen;

  public GuiScreenChangedEventData(GuiScreen fromScreen, GuiScreen toScreen) {
    this.fromScreen = fromScreen;
    this.toScreen = toScreen;
  }

  public enum ListenType {
    OPEN_ONLY,
    CLOSE_ONLY,
    OPEN_AND_CLOSE
  }
}
