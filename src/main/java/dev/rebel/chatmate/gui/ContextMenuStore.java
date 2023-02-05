package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.events.models.MouseEventOptions;
import dev.rebel.chatmate.gui.ContextMenu.ContextMenuOption;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.events.ForgeEventService;
import dev.rebel.chatmate.events.MouseEventService;
import dev.rebel.chatmate.events.models.GuiScreenChangedEventData;
import dev.rebel.chatmate.events.models.MouseEventData;
import net.minecraft.client.Minecraft;

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
  private MouseEventData.MousePositionData latestPositionData;
  private boolean setClear = false;

  public ContextMenuStore(Minecraft minecraft, ForgeEventService forgeEventService, MouseEventService mouseEventService, DimFactory dimFactory, FontEngine fontEngine) {
    this.minecraft = minecraft;
    this.forgeEventService = forgeEventService;
    this.mouseEventService = mouseEventService;
    this.dimFactory = dimFactory;
    this.fontEngine = fontEngine;

    this.forgeEventService.onRenderTick(this::onRender);
    this.forgeEventService.onGuiScreenChanged(this::onGuiScreenChanged, null);
    this.mouseEventService.on(MouseEventService.MouseEventType.MOUSE_MOVE, this::onMouseMove, new MouseEventOptions(), null);
    this.mouseEventService.on(MouseEventService.MouseEventType.MOUSE_DOWN, this::onMouseDown, new MouseEventOptions(), null);
  }

  private void onGuiScreenChanged(Event<GuiScreenChangedEventData> event) {
    this.clearContextMenu();
  }

  private void onMouseDown(Event<MouseEventData> event) {
    if (this.currentMenu == null) {
      return;
    }

    MouseEventData data = event.getData();
    if (data.mouseButtonData.eventButton != MouseButton.LEFT_BUTTON) {
      this.clearContextMenu();
      return;
    }

    if (this.currentMenu.handleClick(data.mousePositionData.x, data.mousePositionData.y)) {
      this.clearContextMenu();
      event.stopPropagation();
    } else {
      this.clearContextMenu();
    }
  }

  private void onMouseMove(Event<MouseEventData> event) {
    // todo: have mouseEventService store the current position, so it can be fetched on-demand
    this.latestPositionData = event.getData().mousePositionData;
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

  private void onRender(Event<?> event) {
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
  }
}
