package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudScreen;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.hud.IHudComponent;
import dev.rebel.chatmate.gui.hud.ServerLogsTimeSeriesComponent;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.ContextMenuService;
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
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
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

  private @Nullable MousePositionData mousePositionData = null;
  private @Nullable IHudComponent draggingComponent = null;
  private @Nullable Dim draggingComponentOffsetX = null;
  private @Nullable Dim draggingComponentOffsetY = null;

  public GuiChatMateHudScreen(Minecraft minecraft, MouseEventService mouseEventService, DimFactory dimFactory, GuiChatMateHud hud) {
    super();

    this.minecraft = minecraft;
    this.mouseEventService = mouseEventService;
    this.dimFactory = dimFactory;
    this.guiChatMateHud = hud;

    Options options = new Options(false, MouseButton.LEFT_BUTTON, MouseButton.RIGHT_BUTTON);
    this.mouseEventService.on(Events.MOUSE_DOWN, this.onMouseDown, options, this);
    this.mouseEventService.on(Events.MOUSE_MOVE, this.onMouseMove, options, this);
    this.mouseEventService.on(Events.MOUSE_UP, this.onMouseUp, options, this);
    this.mouseEventService.on(Events.MOUSE_SCROLL, this.onMouseScroll, options, this);
  }

  @Override
  public void onResize(Minecraft minecraft, int w, int h) {
    super.onResize(minecraft, w, h);

    // todo: allow anchoring to side/corner, so that the element's position always stays constant relative to that side/corner.
    // currently, the implied anchor is top-left.
    for (IHudComponent component : this.guiChatMateHud.hudComponents) {
      if (component instanceof ServerLogsTimeSeriesComponent) {
        ((ServerLogsTimeSeriesComponent)component).onMinecraftResize();
      }
    }
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

  /** To be called when the HUD screen is active, and just before the HUD components are being rendered. */
  public void renderGameOverlayPreHud() {
    if (this.mousePositionData == null || this.minecraft.currentScreen != this && !(this.minecraft.currentScreen instanceof ChatMateHudScreen)) {
      return;
    }

    for (IHudComponent component : this.guiChatMateHud.hudComponents) {
      if (containsPoint(component, this.mousePositionData.point)) {
        if (component.canTranslate() || component.canResizeBox()) {
          DimRect rect = new DimRect(component.getX(), component.getY(), component.getWidth(), component.getHeight());
          float alpha = this.draggingComponent == component ? 0.2f : 0.1f;
          RendererHelpers.drawRect(0, rect, Colour.BLACK.withAlpha(alpha));
        }
      }
    }
  }

  /** To be called when the HUD screen is active, and just after the HUD components are being rendered. */
  public void renderGameOverlayPostHud() {

  }

  private Out onMouseDown(In in) {
    if (this.minecraft.currentScreen != this && !(this.minecraft.currentScreen instanceof ChatMateHudScreen)) {
      // this can happen if we are not displaying, but another object references us (e.g. as a parent screen)
      // so we haven't unsubscribed from the mouse events.
      return new Out(null);
    }

    MousePositionData position = in.mousePositionData.setAnchor(Dim.DimAnchor.GUI);

    if (in.mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      for (IHudComponent component : this.getReverseComponents()) {
        if (component.canTranslate() && containsPoint(component, new DimPoint(position.x, position.y))) {
          this.draggingComponent = component;

          // the position of the component is unlikely to be where we actually start the drag - calculate the offset
          this.draggingComponentOffsetX = position.x.minus(component.getX());
          this.draggingComponentOffsetY = position.y.minus(component.getY());
          return new Out(Out.MouseHandlerAction.HANDLED);
        }
      }
    }
    return new Out(null);
  }

  private Out onMouseMove(In in) {
    this.mousePositionData = in.mousePositionData.setAnchor(Dim.DimAnchor.GUI);

    if (this.minecraft.currentScreen != this && !(this.minecraft.currentScreen instanceof ChatMateHudScreen)) {
      return new Out(null);
    }

    if (in.isDragged(MouseButton.LEFT_BUTTON) && this.draggingComponent != null) {
      // note that we are not checking if the mouse is still hovering over the component
      assert this.mousePositionData != null;
      Dim newX = this.mousePositionData.x.minus(this.draggingComponentOffsetX);
      Dim newY = this.mousePositionData.y.minus(this.draggingComponentOffsetY);
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
    if (this.minecraft.currentScreen != this && !(this.minecraft.currentScreen instanceof ChatMateHudScreen)) {
      return new Out(null);
    }

    MousePositionData position = in.mousePositionData.setAnchor(Dim.DimAnchor.GUI);
    for (IHudComponent component : this.getReverseComponents()) {
      if (component.canRescaleContent() && containsPoint(component, new DimPoint(position.x, position.y))) {
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

  private static boolean containsPoint(IHudComponent component, DimPoint point) {
    return new DimRect(component.getX(), component.getY(), component.getWidth(), component.getHeight()).checkCollision(point);
  }
}
