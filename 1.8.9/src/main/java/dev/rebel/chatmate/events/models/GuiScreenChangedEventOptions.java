package dev.rebel.chatmate.events.models;

import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

public class GuiScreenChangedEventOptions {
  public final GuiScreenChangedEventData.ListenType listenType;

  /** If null, will be interpreted as the "empty screen". */
  public final @Nullable Class<? extends GuiScreen> screenFilter;

  public GuiScreenChangedEventOptions(GuiScreenChangedEventData.ListenType listenType, @Nullable Class<? extends GuiScreen> screenFilter) {
    this.listenType = listenType;
    this.screenFilter = screenFilter;
  }
}
