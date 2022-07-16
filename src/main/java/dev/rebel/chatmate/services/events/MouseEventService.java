package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.MouseEventService.Events;
import dev.rebel.chatmate.services.events.models.InputEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.*;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MousePositionData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseScrollData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import dev.rebel.chatmate.services.events.models.Tick;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class MouseEventService extends EventServiceBase<Events> {
  private final ForgeEventService forgeEventService;
  private final Minecraft minecraft;
  private final DimFactory dimFactory;

  private @Nonnull MousePositionData prevPosition;
  private Set<MouseButton> prevHeld = new HashSet<>();

  public MouseEventService(LogService logService, ForgeEventService forgeEventService, Minecraft minecraft, DimFactory dimFactory) {
    super(Events.class, logService);
    this.forgeEventService = forgeEventService;
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;

    this.prevPosition = new MousePositionData(dimFactory.fromScreen(0), dimFactory.fromScreen(0));

    this.forgeEventService.onGuiScreenMouse(this::onGuiScreenMouse, new InputEventData.Options());
  }

  public void on(Events event, Function<In, Out> handler, Options options, Object key) {
    this.addListener(event, handler, options, key);
  }

  public boolean off(Events event, Object key) {
    return this.removeListener(event, key);
  }

  public MousePositionData getCurrentPosition() {
    return this.prevPosition;
  }

  private InputEventData.Out onGuiScreenMouse(InputEventData.In in) {
    // always use getEvent* so it doesn't reset its internal state after the call.

    // the button action could have occurred at the beginning or end of the mouse move sequence, so must keep track of position
    Map<MouseButton, MousePositionData> buttonsPressed = new HashMap<>();
    Map<MouseButton, MousePositionData> buttonsReleased = new HashMap<>();
    MousePositionData position = null;
    int dWheel = 0;

    // collect event data // note: since we don't have direct access to the mouse event bus, there will only ever be one event at a time
//    while (Mouse.next()) {
      int nextX = Mouse.getEventX();
      int nextY = Mouse.getEventY();
      position = this.constructPositionData(nextX, nextY);

      dWheel += Mouse.getEventDWheel();

      int eventButton = Mouse.getEventButton();
      if (eventButton >= 0) {
        MouseButton button = eventButton == 0 ? MouseButton.LEFT_BUTTON : eventButton == 1 ? MouseButton.RIGHT_BUTTON : eventButton == 2 ? MouseButton.SCROLL_BUTTON : MouseButton.UNKNOWN;
        if (Mouse.getEventButtonState()) {
          buttonsPressed.put(button, position);
        } else {
          buttonsReleased.put(button, position);
        }
      }
//    }

    if (position == null) {
      return new InputEventData.Out(false);
    }

    // cancel out ups/downs
    for (MouseButton button : buttonsPressed.keySet()) {
      if (buttonsReleased.containsKey(button)) {
        buttonsReleased.remove(button);
        buttonsPressed.remove(button);
      }
    }
    Set<MouseButton> currentDown = new HashSet<>(prevHeld);
    boolean swallowed = false;

    // fire button events that started at the beginning of the sequence
    for (MouseButton button : buttonsReleased.keySet()) {
      currentDown.remove(button);
      MousePositionData buttonPosition = buttonsReleased.get(button);
      if (buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(Events.MOUSE_UP, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          swallowed = true;
        }
      }
    }
    for (MouseButton button : buttonsPressed.keySet()) {
      currentDown.add(button);
      MousePositionData buttonPosition = buttonsPressed.get(button);
      if (buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(Events.MOUSE_DOWN, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          swallowed = true;
        }
      }
    }

    // fire move event
    if (!position.equals(this.prevPosition)) {
      if (this.dispatchEvent(Events.MOUSE_MOVE, position, new MouseButtonData(null, currentDown), null)) {
        swallowed = true;
      }
    }

    // fire remaining button events
    for (MouseButton button : buttonsReleased.keySet()) {
      currentDown.remove(button);
      MousePositionData buttonPosition = buttonsReleased.get(button);
      if (!buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(Events.MOUSE_UP, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          swallowed = true;
        }
      }
    }
    for (MouseButton button : buttonsPressed.keySet()) {
      currentDown.add(button);
      MousePositionData buttonPosition = buttonsPressed.get(button);
      if (!buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(Events.MOUSE_DOWN, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          swallowed = true;
        }
      }
    }

    // fire scroll wheel event
    if (dWheel != 0) {
      ScrollDirection direction = dWheel < 0 ? ScrollDirection.DOWN : ScrollDirection.UP;
      MouseScrollData scrollData = new MouseScrollData(direction, Math.abs(dWheel));
      if (this.dispatchEvent(Events.MOUSE_SCROLL, position, new MouseButtonData(null, currentDown), scrollData)) {
        swallowed = true;
      }
    }

    // update state
    this.prevPosition = position;
    this.prevHeld = new HashSet<>(currentDown);
    return new InputEventData.Out(swallowed);
  }

  private MousePositionData constructPositionData(int rawMouseX, int rawMouseY) {
    Dim x = this.dimFactory.fromScreen(rawMouseX);
    Dim y = this.dimFactory.fromScreen(this.minecraft.displayHeight - rawMouseY - 1);

    return new MousePositionData(x, y);
  }

  /** Returns true if the event has been swallowed. */
  private boolean dispatchEvent(Events event, MousePositionData positionData, MouseButtonData buttonData, @Nullable MouseScrollData scrollData) {
    boolean handled = false;

    // to prevent collection concurrency issues (a listener may unsubscribe itself from the list), we first copy the list
    List<EventHandler<In, Out, Options>> listeners = Collections.list(this.getListeners(event, MouseEventData.class));
    for (EventHandler<In, Out, Options> handler : listeners) {
      Options options = handler.options;
      if (options.ignoreHandled && handled) {
        continue;
      } else if ((event == Events.MOUSE_DOWN || event == Events.MOUSE_UP)
          && options.listenForButtons != null
          && !options.listenForButtons.contains(buttonData.eventButton)) {
        continue;
      }

      In eventIn = new In(event, positionData, buttonData, scrollData);
      Out eventOut = this.safeDispatch(event, handler, eventIn);

      if (eventOut == null || eventOut.handlerAction == null) {
        continue;
      } else if (eventOut.handlerAction == MouseHandlerAction.HANDLED) {
        handled = true;
      } else if (eventOut.handlerAction == MouseHandlerAction.SWALLOWED) {
        return true;
      }
    }

    return false;
  }

  public enum Events {
    MOUSE_DOWN,
    MOUSE_MOVE,
    MOUSE_UP,
    MOUSE_SCROLL
  }
}
