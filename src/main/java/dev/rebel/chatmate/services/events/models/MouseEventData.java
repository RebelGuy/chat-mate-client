package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.MouseEventService.Events;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out;
import dev.rebel.chatmate.services.events.models.MouseEventData.Options;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MouseEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final MouseEventService.Events event;

    public final MousePositionData mousePositionData;
    public final MouseButtonData mouseButtonData;

    /** Only defined for MOUSE_SCROLL events. */
    public final @Nullable MouseScrollData mouseScrollData;

    public In(Events event, MousePositionData mousePositionData, MouseButtonData mouseButtonData, @Nullable MouseScrollData mouseScrollData) {
      this.event = event;
      this.mousePositionData = mousePositionData;
      this.mouseButtonData = mouseButtonData;
      this.mouseScrollData = mouseScrollData;
    }

    public boolean isClicked(MouseButton mouseButton) {
      return this.event == Events.MOUSE_DOWN && this.mouseButtonData.eventButton == mouseButton;
    }

    public boolean isDragged(MouseButton mouseButton) {
      return this.event == Events.MOUSE_MOVE && this.mouseButtonData.pressedButtons.contains(mouseButton);
    }

    public boolean isReleased(MouseButton mouseButton) {
      return this.event == Events.MOUSE_UP && this.mouseButtonData.eventButton == mouseButton;
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

  public static class Out extends EventOut {
    public final @Nullable MouseHandlerAction handlerAction;

    public Out() {
      this(null);
    }

    public Out(@Nullable MouseHandlerAction action) {
      this.handlerAction = action;
    }

    public enum MouseHandlerAction {
      /** The mouse action was handled, but allow the event to continue propagation. */
      HANDLED,

      /** The mouse action was handled, and the event should immediately stop propagating. */
      SWALLOWED
    }
  }

  public static class Options extends EventOptions {
    public final boolean ignoreHandled;
    public final @Nullable Set<MouseButton> listenForButtons;

    public Options() {
      this(false);
    }

    public Options(boolean ignoreHandled) {
      this.ignoreHandled = ignoreHandled;
      this.listenForButtons = null;
    }

    /** Only fire button events (`MOUSE_UP`, `MOUSE_DOWN`) for the specified button types. */
    public Options(boolean ignoreHandled, MouseButton... listenForButtons) {
      this.ignoreHandled = ignoreHandled;
      this.listenForButtons = new HashSet<>(Arrays.asList(listenForButtons));
    }
  }
}
