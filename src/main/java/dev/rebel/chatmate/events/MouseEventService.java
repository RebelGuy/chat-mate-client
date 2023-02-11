package dev.rebel.chatmate.events;

import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.events.models.MouseEventData.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.events.models.MouseEventOptions;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.MouseEventService.MouseEventType;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.*;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MouseEventService extends EventServiceBase<MouseEventType> {
  private final ForgeEventService forgeEventService;
  private final Minecraft minecraft;
  private final DimFactory dimFactory;

  private @Nonnull MousePositionData prevPosition;
  private Set<MouseButton> prevHeld = new HashSet<>();

  public MouseEventService(LogService logService, ForgeEventService forgeEventService, Minecraft minecraft, DimFactory dimFactory) {
    super(MouseEventType.class, logService);
    this.forgeEventService = forgeEventService;
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;

    this.prevPosition = new MousePositionData(dimFactory.fromScreen(0), dimFactory.fromScreen(0));

    this.forgeEventService.onGuiScreenMouse(this::onGuiScreenMouse);
    this.forgeEventService.onRenderTick(this::onRenderTick);
  }

  public void on(MouseEventType event, EventCallback<MouseEventData> handler, MouseEventOptions options, Object key) {
    this.addListener(event, handler, options, key);
  }

  public boolean off(MouseEventType event, Object key) {
    return this.removeListener(event, key);
  }

  public MousePositionData getCurrentPosition() {
    return this.prevPosition;
  }

  public MouseEventData constructSyntheticMoveEvent() {
    return new MouseEventData(MouseEventType.MOUSE_MOVE, this.getCurrentPosition(), new MouseButtonData(null, this.prevHeld), null);
  }

  public boolean isHeldDown(MouseButton button) {
    return this.constructSyntheticMoveEvent().mouseButtonData.pressedButtons.contains(button);
  }

  private void onRenderTick(Event<?> event) {
    int x = Mouse.getX();
    int y = Mouse.getY();
    MousePositionData position = this.constructPositionData(x, y);

    if (!this.prevPosition.Equals(position)) {
      this.prevPosition = position;
      Set<MouseButton> currentDown = new HashSet<>(prevHeld);
      this.dispatchEvent(MouseEventType.MOUSE_MOVE, position, new MouseButtonData(null, currentDown), null);
    }
  }

  private void onGuiScreenMouse(Event<?> event) {
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
      return;
    }

    // cancel out ups/downs
    for (MouseButton button : buttonsPressed.keySet()) {
      if (buttonsReleased.containsKey(button)) {
        buttonsReleased.remove(button);
        buttonsPressed.remove(button);
      }
    }
    Set<MouseButton> currentDown = new HashSet<>(prevHeld);
    boolean handled = false;

    // fire button events that started at the beginning of the sequence
    for (MouseButton button : buttonsReleased.keySet()) {
      currentDown.remove(button);
      MousePositionData buttonPosition = buttonsReleased.get(button);
      if (buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(MouseEventType.MOUSE_UP, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          handled = true;
        }
      }
    }
    for (MouseButton button : buttonsPressed.keySet()) {
      currentDown.add(button);
      MousePositionData buttonPosition = buttonsPressed.get(button);
      if (buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(MouseEventType.MOUSE_DOWN, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          handled = true;
        }
      }
    }

    // fire move event
    if (!position.Equals(this.prevPosition)) {
      if (this.dispatchEvent(MouseEventType.MOUSE_MOVE, position, new MouseButtonData(null, currentDown), null)) {
        handled = true;
      }
    }

    // fire remaining button events
    for (MouseButton button : buttonsReleased.keySet()) {
      currentDown.remove(button);
      MousePositionData buttonPosition = buttonsReleased.get(button);
      if (!buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(MouseEventType.MOUSE_UP, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          handled = true;
        }
      }
    }
    for (MouseButton button : buttonsPressed.keySet()) {
      currentDown.add(button);
      MousePositionData buttonPosition = buttonsPressed.get(button);
      if (!buttonPosition.equals(this.prevPosition)) {
        if (this.dispatchEvent(MouseEventType.MOUSE_DOWN, buttonPosition, new MouseButtonData(button, currentDown), null)) {
          handled = true;
        }
      }
    }

    // fire scroll wheel event
    if (dWheel != 0) {
      ScrollDirection direction = dWheel < 0 ? ScrollDirection.DOWN : ScrollDirection.UP;
      MouseScrollData scrollData = new MouseScrollData(direction, Math.abs(dWheel));
      if (this.dispatchEvent(MouseEventType.MOUSE_SCROLL, position, new MouseButtonData(null, currentDown), scrollData)) {
        handled = true;
      }
    }

    // update state
    this.prevPosition = position;
    this.prevHeld = new HashSet<>(currentDown);

    if (handled) {
      event.stopPropagation();
    }
  }

  private MousePositionData constructPositionData(int rawMouseX, int rawMouseY) {
    Dim x = this.dimFactory.fromScreen(rawMouseX);
    Dim y = this.dimFactory.fromScreen(this.minecraft.displayHeight - rawMouseY - 1);

    return new MousePositionData(x, y);
  }

  /** Returns true if the event has been handled. */
  private boolean dispatchEvent(MouseEventType eventType, MousePositionData positionData, MouseButtonData buttonData, @Nullable MouseScrollData scrollData) {
    MouseEventData data = new MouseEventData(eventType, positionData, buttonData, scrollData);
    Event<MouseEventData> event = new Event<>(data);

    // to prevent collection concurrency issues (a listener may unsubscribe itself from the list), we first copy the list
    List<EventHandler<MouseEventData, MouseEventOptions>> listeners = Collections.list(this.getListeners(eventType, MouseEventData.class, MouseEventOptions.class));
    for (EventHandler<MouseEventData, MouseEventOptions> handler : listeners) {
      MouseEventOptions options = handler.options;
      if ((eventType == MouseEventType.MOUSE_DOWN || eventType == MouseEventType.MOUSE_UP)
          && options != null
          && options.listenForButtons != null
          && !options.listenForButtons.contains(buttonData.eventButton)) {
        continue;
      }

      this.safeDispatch(eventType, handler, event);

      if (event.stoppedPropagation) {
        return true;
      }
    }

    return false;
  }

  public enum MouseEventType {
    MOUSE_DOWN,
    MOUSE_MOVE,
    MOUSE_UP,
    MOUSE_SCROLL
  }
}
