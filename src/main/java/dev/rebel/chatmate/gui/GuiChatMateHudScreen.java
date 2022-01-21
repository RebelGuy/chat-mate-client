package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.GuiScreenMouse;
import dev.rebel.chatmate.services.events.models.GuiScreenMouse.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/** This is the focused menu screen - for the game overlay class, go to GuiChatMateHud */
public class GuiChatMateHudScreen extends GuiScreen {
  private final Minecraft minecraft;
  private final GuiChatMateHud guiChatMateHud;
  private final ForgeEventService forgeEventService;

  public GuiChatMateHudScreen(Minecraft minecraft, GuiChatMateHud hud, ForgeEventService forgeEventService) {
    super();

    this.minecraft = minecraft;
    guiChatMateHud = hud;
    this.forgeEventService = forgeEventService;

    this.forgeEventService.onGuiScreenMouse(this, this::onMouse, new Options(GuiChatMateHudScreen.class));
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();

    this.forgeEventService.offGuiScreenMouse(this);
  }

  @Override
  public void initGui() {
    super.initGui();

  }

  @Override
  public void updateScreen() {
    super.updateScreen();
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  private GuiScreenMouse.Out onMouse(GuiScreenMouse.In eventIn) {
    return new GuiScreenMouse.Out(); // Draw a rect around a component when hovering over it, coloured bordered when dragging, etc
  }
}
