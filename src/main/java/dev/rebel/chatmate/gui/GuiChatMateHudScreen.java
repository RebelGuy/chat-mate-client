package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.IHudComponent;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.MouseEventService.Events;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MousePositionData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.services.events.models.MouseEventData.Options;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** This is the focused menu screen - for the game overlay class, go to GuiChatMateHud */
public class GuiChatMateHudScreen extends GuiScreen {
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final GuiChatMateHud guiChatMateHud;
  private final MouseEventService mouseEventService;
  
  private final Function<In, Out> onMouseDown = this::onMouseDown;
  private final Function<In, Out> onMouseMove = this::onMouseMove;
  private final Function<In, Out> onMouseUp = this::onMouseUp;
  private final Function<In, Out> onMouseScroll = this::onMouseScroll;

  private IHudComponent draggingComponent = null;
  private Dim draggingComponentOffsetX = null;
  private Dim draggingComponentOffsetY = null;

  public GuiChatMateHudScreen(Minecraft minecraft, MouseEventService mouseEventService, DimFactory dimFactory, GuiChatMateHud hud) {
    super();

    this.minecraft = minecraft;
    this.mouseEventService = mouseEventService;
    this.dimFactory = dimFactory;
    this.guiChatMateHud = hud;

    Options options = new Options(false, MouseButton.LEFT_BUTTON);
    this.mouseEventService.on(Events.MOUSE_DOWN, this.onMouseDown, options, this);
    this.mouseEventService.on(Events.MOUSE_MOVE, this.onMouseMove, options, this);
    this.mouseEventService.on(Events.MOUSE_UP, this.onMouseUp, options, this);
    this.mouseEventService.on(Events.MOUSE_SCROLL, this.onMouseScroll, options, this);
  }

  @Override
  public void initGui() {
    super.initGui();
  }

  @Override
  public void drawScreen(int x, int y, float partialTicks) {
    super.drawScreen(x, y, partialTicks);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  // todo: draw a rect around a component when hovering over it, coloured bordered when dragging, etc

  private Out onMouseDown(In in) {
    MousePositionData position = in.mousePositionData;
    for (IHudComponent component : this.getReverseComponents()) {
      if (component.canTranslate() && containsPoint(component, position.x, position.y)) {
        this.draggingComponent = component;

        // the position of the component is unlikely to be where we actually start the drag - calculate the offset
        this.draggingComponentOffsetX = position.x.minus(component.getX());
        this.draggingComponentOffsetY = position.y.minus(component.getY());
        break;
      }
    }
    return new Out(null);
  }

  private Out onMouseMove(In in) {
    MousePositionData position = in.mousePositionData;
    if (in.isDragged(MouseButton.LEFT_BUTTON) && this.draggingComponent != null) {
      // note that we are not checking if the mouse is still hovering over the component
      Dim newX = position.x.minus(this.draggingComponentOffsetX);
      Dim newY = position.y.minus(this.draggingComponentOffsetY);
      this.draggingComponent.onTranslate(newX, newY);
    }

    return new Out(null);
  }

  private Out onMouseUp(In in) {
    this.draggingComponent = null;
    this.draggingComponentOffsetX = null;
    this.draggingComponentOffsetY = null;
    return new Out(null);
  }

  private Out onMouseScroll(In in) {
    MousePositionData position = in.mousePositionData;
    for (IHudComponent component : this.getReverseComponents()) {
      if (component.canRescaleContent() && containsPoint(component, position.x, position.y)) {
        int multiplier = in.mouseScrollData.scrollDirection == ScrollDirection.UP ? 1 : -1;
        component.onRescaleContent(component.getContentScale() + multiplier * 0.1f);
      }
    }
    return new Out(null);
  }

  /** Since components are drawn in order, any UI collision tests should start checking from the topmost item. */
  private List<IHudComponent> getReverseComponents() {
    List<IHudComponent> components = new ArrayList<>(this.guiChatMateHud.hudComponents);
    Collections.reverse(components);
    return components;
  }

  private static boolean containsPoint(IHudComponent component, Dim x, Dim y) {
    Dim left = component.getX();
    Dim right = component.getX().plus(component.getWidth());
    Dim top = component.getY();
    Dim bottom = component.getY().plus(component.getHeight());
    return x.gte(left) && x.lte(right) && y.gte(top) && y.lte(bottom);
  }
}
