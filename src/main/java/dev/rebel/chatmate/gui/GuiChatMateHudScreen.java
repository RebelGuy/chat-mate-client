package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.IHudComponent;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.GuiScreenMouse;
import dev.rebel.chatmate.services.events.models.GuiScreenMouse.Options;
import dev.rebel.chatmate.services.events.models.GuiScreenMouse.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/** This is the focused menu screen - for the game overlay class, go to GuiChatMateHud */
public class GuiChatMateHudScreen extends GuiScreen {
  private final Minecraft minecraft;
  private final GuiChatMateHud guiChatMateHud;
  private final ForgeEventService forgeEventService;

  private IHudComponent draggingComponent = null;
  private Integer draggingComponentOffsetX = null;
  private Integer draggingComponentOffsetY = null;

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
    if (eventIn.type == Type.MOVE && eventIn.isDragging && this.draggingComponent != null) {
      // note that we are not checking if the mouse is still hovering over the component
      int newX = eventIn.currentX - this.draggingComponentOffsetX;
      int newY = eventIn.currentY - this.draggingComponentOffsetY;
      this.draggingComponent.onTranslate(newX, newY);

    } else if (eventIn.type == Type.DOWN) {
      for (IHudComponent component : this.guiChatMateHud.hudComponents) {
        if (component.canTranslate() && containsPoint(component, eventIn.startX, eventIn.startY)) {
          this.draggingComponent = component;

          // the position of the component is unlikely to be where we actually start the drag - calculate the offset
          this.draggingComponentOffsetX = eventIn.startX - component.getX();
          this.draggingComponentOffsetY = eventIn.startY - component.getY();
          break;
        }
      }

    } else if (eventIn.type == Type.UP) {
      this.draggingComponent = null;
      this.draggingComponentOffsetX = null;
      this.draggingComponentOffsetY = null;
    }


    return new GuiScreenMouse.Out(); // Draw a rect around a component when hovering over it, coloured bordered when dragging, etc
  }

  private static boolean containsPoint(IHudComponent component, int x, int y) {
    return x <= component.getX() + component.getWidth()
        && x >= component.getX()
        && y <= component.getY() + component.getHeight()
        && y >= component.getY();
  }
}
