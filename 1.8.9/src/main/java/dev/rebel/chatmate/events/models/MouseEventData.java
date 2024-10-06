package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.events.MouseEventService.MouseEventType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MouseEventData {
  public final MouseEventType event;

  public final MousePositionData mousePositionData;
  public final MouseButtonData mouseButtonData;

  /** Only defined for MOUSE_SCROLL events. */
  public final @Nullable MouseScrollData mouseScrollData;

  public MouseEventData(MouseEventType event, MousePositionData mousePositionData, MouseButtonData mouseButtonData, @Nullable MouseScrollData mouseScrollData) {
    this.event = event;
    this.mousePositionData = mousePositionData;
    this.mouseButtonData = mouseButtonData;
    this.mouseScrollData = mouseScrollData;
  }

  public boolean isClicked(MouseButton mouseButton) {
    return this.event == MouseEventType.MOUSE_DOWN && this.mouseButtonData.eventButton == mouseButton;
  }

  public boolean isDragged(MouseButton mouseButton) {
    return this.event == MouseEventType.MOUSE_MOVE && this.mouseButtonData.pressedButtons.contains(mouseButton);
  }

  public boolean isReleased(MouseButton mouseButton) {
    return this.event == MouseEventType.MOUSE_UP && this.mouseButtonData.eventButton == mouseButton;
  }

  public static class MouseScrollData {
    public final ScrollDirection scrollDirection;
    public final int scrollDistance;

    public MouseScrollData(ScrollDirection scrollDirection, int scrollDistance) {
      this.scrollDirection = scrollDirection;
      this.scrollDistance = scrollDistance;
    }

    public enum ScrollDirection {
      UP,
      DOWN
    }
  }

  public static class MousePositionData {
    /** The rounded position of the mouse in GUI coordinates (possibly scaled) */
    public final Dim x;
    public final Dim y;
    public final DimPoint point;

    public MousePositionData(Dim x, Dim y) {
      this.x = x.copy();
      this.y = y.copy();
      this.point = new DimPoint(x, y);
    }

    public boolean Equals(MousePositionData other) {
      return other == null || this.x.equals(other.x) && this.y.equals(other.y);
    }

    public MousePositionData setAnchor(Dim.DimAnchor anchor) {
      return new MousePositionData(this.x.setAnchor(anchor), this.y.setAnchor(anchor));
    }
  }

  public static class MouseButtonData {
    /** The button related to the event (only for MOUSE_DOWN or MOUSE_UP) */
    public final @Nullable MouseButton eventButton;

    /** The set of buttons currently pressed. */
    public final Set<MouseButton> pressedButtons;

    public MouseButtonData(@Nullable MouseButton eventButton, Set<MouseButton> pressedButtons) {
      this.eventButton = eventButton;
      this.pressedButtons = new HashSet<>(pressedButtons);
    }

    public enum MouseButton {
      LEFT_BUTTON,
      RIGHT_BUTTON,
      SCROLL_BUTTON,
      UNKNOWN
    }
  }
}
