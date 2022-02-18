package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.ContextMenu.ContextMenuOption;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay;
import dev.rebel.chatmate.services.events.models.Tick;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import scala.Tuple2;

public class ContextMenuStore {
  private final Minecraft minecraft;
  private final ForgeEventService forgeEventService;
  private final MouseEventService mouseEventService;
  private final DimFactory dimFactory;

  private ContextMenu currentMenu;
  private MouseEventData.In.MousePositionData latestPositionData;
  private boolean setClear = false;

  public ContextMenuStore(Minecraft minecraft, ForgeEventService forgeEventService, MouseEventService mouseEventService, DimFactory dimFactory) {
    this.minecraft = minecraft;
    this.forgeEventService = forgeEventService;
    this.mouseEventService = mouseEventService;
    this.dimFactory = dimFactory;

    this.forgeEventService.onRenderTick(this::onRender, null);
    this.mouseEventService.on(MouseEventService.Events.MOUSE_MOVE, this::onMouseMove, new MouseEventData.Options(), null);
    this.mouseEventService.on(MouseEventService.Events.MOUSE_DOWN, this::onMouseDown, new MouseEventData.Options(), null);
  }

  private MouseEventData.Out onMouseDown(MouseEventData.In in) {
    if (this.currentMenu == null) {
      return new MouseEventData.Out();
    }

    if (in.mouseButtonData.eventButton != MouseButton.LEFT_BUTTON) {
      this.clearContextMenu();
      return new MouseEventData.Out();
    }

    if (this.currentMenu.handleClick(in.mousePositionData.x, in.mousePositionData.y)) {
      this.clearContextMenu();
      return new MouseEventData.Out(MouseHandlerAction.SWALLOWED);
    } else {
      this.clearContextMenu();
      return new MouseEventData.Out();
    }
  }

  private MouseEventData.Out onMouseMove(MouseEventData.In in) {
    // todo: have mouseEventService store the current position, so it can be fetched on-demand
    this.latestPositionData = in.mousePositionData;
    return new MouseEventData.Out();
  }


  public void showContextMenu(Dim mouseX, Dim mouseY, ContextMenuOption... options) {
    this.currentMenu = new ContextMenu(mouseX, mouseY, options);
  }

  public void clearContextMenu() {
    // don't immediately set this to null, finish the current game loop first.
    // this way, the state will be consistent during the current game step.
    this.setClear = true;
  }

  public boolean isShowingContextMenu() {
    return this.currentMenu != null;
  }

  private Tick.Out onRender(Tick.In in) {
    if (this.setClear) {
      this.setClear = false;
      this.currentMenu = null;
    }

    Tuple2<Dim, Dim> minecraftDim = this.dimFactory.getMinecraftDim();
    int width = (int)minecraftDim._1.getGui();
    int height = (int)minecraftDim._2.getGui();
    int maxWidth = width / 2;
    if (this.currentMenu != null && this.latestPositionData != null) {
      this.currentMenu.drawMenu(this.latestPositionData.x, this.latestPositionData.y, width, height, maxWidth, this.minecraft.fontRendererObj);
    }

    return new Tick.Out();
  }
}
