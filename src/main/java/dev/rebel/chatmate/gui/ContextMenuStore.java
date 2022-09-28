package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.ContextMenu.ContextMenuOption;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.models.GuiScreenChanged;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import dev.rebel.chatmate.services.events.models.Tick;
import net.minecraft.client.Minecraft;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class ContextMenuStore {
  private final Minecraft minecraft;
  private final ForgeEventService forgeEventService;
  private final MouseEventService mouseEventService;
  private final DimFactory dimFactory;
  private final FontEngine fontEngine;

  private ContextMenu currentMenu;
  private MouseEventData.In.MousePositionData latestPositionData;
  private boolean setClear = false;

  public ContextMenuStore(Minecraft minecraft, ForgeEventService forgeEventService, MouseEventService mouseEventService, DimFactory dimFactory, FontEngine fontEngine) {
    this.minecraft = minecraft;
    this.forgeEventService = forgeEventService;
    this.mouseEventService = mouseEventService;
    this.dimFactory = dimFactory;
    this.fontEngine = fontEngine;

    this.forgeEventService.onRenderTick(this::onRender, null);
    this.forgeEventService.onGuiScreenChanged(this::onGuiScreenChanged, null);
    this.mouseEventService.on(MouseEventService.Events.MOUSE_MOVE, this::onMouseMove, new MouseEventData.Options(), null);
    this.mouseEventService.on(MouseEventService.Events.MOUSE_DOWN, this::onMouseDown, new MouseEventData.Options(), null);
  }

  private GuiScreenChanged.Out onGuiScreenChanged(GuiScreenChanged.In in) {
    this.clearContextMenu();
    return new GuiScreenChanged.Out();
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

  public void showContextMenu(Dim x, Dim y, @Nullable ContextMenuOption... options) {
    ContextMenuOption[] nonNullOptions = Arrays.stream(options).filter(Objects::nonNull).toArray(ContextMenuOption[]::new); // yuck
    this.currentMenu = new ContextMenu(this.dimFactory, x, y, nonNullOptions);
    this.setClear = false;
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

    DimPoint minecraftDim = this.dimFactory.getMinecraftSize();
    int width = (int)minecraftDim.getX().getGui();
    int height = (int)minecraftDim.getY().getGui();
    int maxWidth = width / 2;
    if (this.currentMenu != null && this.latestPositionData != null) {
      this.currentMenu.drawMenu(this.latestPositionData.x, this.latestPositionData.y, width, height, maxWidth, this.fontEngine);
    }

    return new Tick.Out();
  }
}
