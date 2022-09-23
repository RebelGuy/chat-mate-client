package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.KeyboardEventService.Events;
import dev.rebel.chatmate.services.events.models.InputEventData;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.In;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.In.KeyModifier;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.Options;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.Out;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.Out.KeyboardHandlerAction;
import dev.rebel.chatmate.services.events.models.Tick;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class KeyboardEventService extends EventServiceBase<Events> {
  private final ForgeEventService forgeEventService;

  private Map<Integer, Character> currentlyHeldDown = new HashMap<>();

  public KeyboardEventService(LogService logService, ForgeEventService forgeEventService) {
    super(Events.class, logService);
    this.forgeEventService = forgeEventService;

    // it's possible onRenderTick is not the correct event to listen to - if it doesn't work, try
    // MouseInputEvent (fires in GuiScreen). Same with Mouse. If so, make sure you override event propagation.
    this.forgeEventService.onGuiScreenKeyboard(this::onGuiScreenKeyboard, null);
  }

  /** Note that multiple events may be emitted at the same time. For now, repeat keys (holding down keys) do not fire multiple events. */
  public void on(Events event, Function<In, Out> handler, Options options, Object key) {
    this.addListener(event, handler, options, key);
  }

  public boolean off(Events event, Object key) {
    return this.removeListener(event, key);
  }

  public boolean isHeldDown(int key) {
    return this.currentlyHeldDown.containsKey(key);
  }

  private InputEventData.Out onGuiScreenKeyboard(InputEventData.In in) {
    int key = Keyboard.getEventKey();
    char character = Keyboard.getEventCharacter();

    Events event;
    if (Keyboard.getEventKeyState()) {
      event = Events.KEY_DOWN;
      this.currentlyHeldDown.put(key, character);
    } else {
      event = Events.KEY_UP;
      this.currentlyHeldDown.remove(key);
    }

    boolean handled = false;
    boolean swallowed = false;
    for (EventHandler<In, Out, Options> handler : this.getListeners(event, KeyboardEventData.class)) {
      Options options = handler.options;
      In eventIn = new In(event, key, character, this.currentlyHeldDown);

      if (options.ignoreHandled && handled
        || options.listenForKeys != null && !options.listenForKeys.contains(key)
        || Boolean.TRUE.equals(options.requireShift) && !eventIn.isKeyModifierActive(KeyModifier.SHIFT)
        || Boolean.FALSE.equals(options.requireShift) && eventIn.isKeyModifierActive(KeyModifier.SHIFT)
        || Boolean.TRUE.equals(options.requireCtrl) && !eventIn.isKeyModifierActive(KeyModifier.CTRL)
        || Boolean.FALSE.equals(options.requireCtrl) && eventIn.isKeyModifierActive(KeyModifier.CTRL)
        || Boolean.TRUE.equals(options.requireAlt) && !eventIn.isKeyModifierActive(KeyModifier.ALT)
        || Boolean.FALSE.equals(options.requireAlt) && eventIn.isKeyModifierActive(KeyModifier.ALT)) {
        continue;
      }

      Out eventOut = this.safeDispatch(event, handler, eventIn);

      if (eventOut.handlerAction == null) {
        continue;
      } else if (eventOut.handlerAction == KeyboardHandlerAction.HANDLED) {
        handled = true;
      } else if (eventOut.handlerAction == KeyboardHandlerAction.SWALLOWED) {
        swallowed = true;
        break;
      }
    }

    return new InputEventData.Out(swallowed);
  }

  public enum Events {
    KEY_UP,
    KEY_DOWN
  }
}
