package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.services.events.MouseEventService.Events;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.*;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MousePositionData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseScrollData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import dev.rebel.chatmate.services.events.models.Tick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MouseEventService extends EventServiceBase<Events> {
  private final ForgeEventService forgeEventService;
  private final Minecraft minecraft;

  private MousePositionData prevPosition = null;
  private Set<MouseButton> prevHeld = new HashSet<>();

  public MouseEventService(ForgeEventService forgeEventService, Minecraft minecraft) {
    super(Events.class);
    this.forgeEventService = forgeEventService;
    this.minecraft = minecraft;

    this.forgeEventService.onRenderTick(this::onRenderTick, null);
  }

  /** Note that multiple events may be emitted at the same time, but they occur in the order such that the events'
   * position data is congruent. Order: `MOUSE_UP` -> `MOUSE_DOWN` -> `MOUSE_MOVE` -> `MOUSE_UP` -> `MOUSE_DOWN` ->
   * `MOUSE_SCROLL`. */
  public void on(Events event, Function<In, Out> handler, Options options, Object key) {
    this.addListener(event, handler, options, key);
  }

  public boolean off(Events event, Object key) {
    return this.removeListener(event, key);
  }

  private Tick.Out onRenderTick(Tick.In in) {
    // always use getEvent* so it doesn't reset its internal state after the call.

    // the button action could have occurred at the beginning or end of the mouse move sequence, so must keep track of position
    Map<MouseButton, MousePositionData> buttonsPressed = new HashMap<>();
    Map<MouseButton, MousePositionData> buttonsReleased = new HashMap<>();
    MousePositionData position = null;
    int dWheel = 0;

    // collect event data
    while (Mouse.next()) {
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
    }

    if (position == null) {
      return new Tick.Out();
    }

    // cancel out ups/downs
    for (MouseButton button : buttonsPressed.keySet()) {
      if (buttonsReleased.containsKey(button)) {
        buttonsReleased.remove(button);
        buttonsPressed.remove(button);
      }
    }
    Set<MouseButton> currentDown = new HashSet<>(prevHeld);

    // fire button events that started at the beginning of the sequence
    for (MouseButton button : buttonsReleased.keySet()) {
      currentDown.remove(button);
      MousePositionData buttonPosition = buttonsReleased.get(button);
      if (buttonPosition.equals(this.prevPosition)) {
        this.dispatchEvent(Events.MOUSE_UP, buttonPosition, new MouseButtonData(button, currentDown), null);
      }
    }
    for (MouseButton button : buttonsPressed.keySet()) {
      currentDown.add(button);
      MousePositionData buttonPosition = buttonsPressed.get(button);
      if (buttonPosition.equals(this.prevPosition)) {
        this.dispatchEvent(Events.MOUSE_DOWN, buttonPosition, new MouseButtonData(button, currentDown), null);
      }
    }

    // fire move event
    if (!position.equals(prevPosition)) {
      this.dispatchEvent(Events.MOUSE_MOVE, position, new MouseButtonData(null, currentDown), null);
    }

    // fire remaining button events
    for (MouseButton button : buttonsReleased.keySet()) {
      currentDown.remove(button);
      MousePositionData buttonPosition = buttonsReleased.get(button);
      if (!buttonPosition.equals(this.prevPosition)) {
        this.dispatchEvent(Events.MOUSE_UP, buttonPosition, new MouseButtonData(button, currentDown), null);
      }
    }
    for (MouseButton button : buttonsPressed.keySet()) {
      currentDown.add(button);
      MousePositionData buttonPosition = buttonsPressed.get(button);
      if (!buttonPosition.equals(this.prevPosition)) {
        this.dispatchEvent(Events.MOUSE_DOWN, buttonPosition, new MouseButtonData(button, currentDown), null);
      }
    }

    // fire scroll wheel event
    if (dWheel != 0) {
      ScrollDirection direction = dWheel < 0 ? ScrollDirection.DOWN : ScrollDirection.UP;
      MouseScrollData scrollData = new MouseScrollData(direction, Math.abs(dWheel));
      this.dispatchEvent(Events.MOUSE_SCROLL, position, new MouseButtonData(null, currentDown), scrollData);
    }

    // update state
    this.prevPosition = position;
    this.prevHeld = new HashSet<>(currentDown);
    return new Tick.Out();
  }

  private MousePositionData constructPositionData(int rawMouseX, int rawMouseY) {
    int screenX = rawMouseX;
    int screenY = this.minecraft.displayHeight - rawMouseY - 1;

    int scale = new ScaledResolution(this.minecraft).getScaleFactor();
    float x = screenX / (float)scale;
    float y = screenY / (float)scale;
    int clientX = (int)x;
    int clientY = (int)y;

    return new MousePositionData(clientX, clientY, x, y, screenX, screenY);
  }

  private void dispatchEvent(Events event, MousePositionData positionData, MouseButtonData buttonData, @Nullable MouseScrollData scrollData) {
    boolean handled = false;
    for (EventHandler<In, Out, Options> handler : this.getListeners(event, MouseEventData.class)) {
      Options options = handler.options;
      if (options.ignoreHandled && handled) {
        continue;
      } else if ((event == Events.MOUSE_DOWN || event == Events.MOUSE_UP)
          && options.listenForButtons != null
          && !options.listenForButtons.contains(buttonData.eventButton)) {
        continue;
      }

      In eventIn = new In(event, positionData, buttonData, null);
      Out eventOut = handler.callback.apply(eventIn);

      if (eventOut.handlerAction == null) {
        continue;
      } else if (eventOut.handlerAction == MouseHandlerAction.HANDLED) {
        handled = true;
      } else if (eventOut.handlerAction == MouseHandlerAction.SWALLOWED) {
        return;
      }
    }
  }

  public enum Events {
    MOUSE_DOWN,
    MOUSE_MOVE,
    MOUSE_UP,
    MOUSE_SCROLL
  }
}
