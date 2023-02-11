package dev.rebel.chatmate.events;

import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.events.KeyboardEventService.KeyboardEventType;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.KeyboardEventData.KeyModifier;
import dev.rebel.chatmate.events.models.KeyboardEventOptions;
import dev.rebel.chatmate.services.LogService;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class KeyboardEventService extends EventServiceBase<KeyboardEventType> {
  private final ForgeEventService forgeEventService;

  private Map<Integer, Character> currentlyHeldDown = new HashMap<>();

  public KeyboardEventService(LogService logService, ForgeEventService forgeEventService) {
    super(KeyboardEventType.class, logService);
    this.forgeEventService = forgeEventService;

    // it's possible onRenderTick is not the correct event to listen to - if it doesn't work, try
    // MouseInputEvent (fires in GuiScreen). Same with Mouse. If so, make sure you override event propagation.
    this.forgeEventService.onGuiScreenKeyboard(this::onGuiScreenKeyboard);
  }

  /** Note that multiple events may be emitted at the same time. For now, repeat keys (holding down keys) do not fire multiple events. */
  public void on(KeyboardEventType eventType, EventCallback<KeyboardEventData> handler, KeyboardEventOptions options, Object key) {
    this.addListener(eventType, handler, options, key);
  }

  public boolean off(KeyboardEventType eventType, Object key) {
    return this.removeListener(eventType, key);
  }

  public boolean isHeldDown(int key) {
    return this.currentlyHeldDown.containsKey(key);
  }

  private void onGuiScreenKeyboard(Event<?> discard) {
    int key = Keyboard.getEventKey();
    char character = Keyboard.getEventCharacter();

    KeyboardEventType eventType;
    if (Keyboard.getEventKeyState()) {
      eventType = KeyboardEventType.KEY_DOWN;
      this.currentlyHeldDown.put(key, character);
    } else {
      eventType = KeyboardEventType.KEY_UP;
      this.currentlyHeldDown.remove(key);
    }

    KeyboardEventData data = new KeyboardEventData(eventType, key, character, this.currentlyHeldDown);
    Event<KeyboardEventData> event = new Event<>(data);

    for (EventHandler<KeyboardEventData, KeyboardEventOptions> handler : this.getListeners(eventType, KeyboardEventData.class, KeyboardEventOptions.class)) {
      KeyboardEventOptions options = handler.options;

      if (options.listenForKeys != null && !options.listenForKeys.contains(key)
        || Boolean.TRUE.equals(options.requireShift) && !data.isKeyModifierActive(KeyModifier.SHIFT)
        || Boolean.FALSE.equals(options.requireShift) && data.isKeyModifierActive(KeyModifier.SHIFT)
        || Boolean.TRUE.equals(options.requireCtrl) && !data.isKeyModifierActive(KeyModifier.CTRL)
        || Boolean.FALSE.equals(options.requireCtrl) && data.isKeyModifierActive(KeyModifier.CTRL)
        || Boolean.TRUE.equals(options.requireAlt) && !data.isKeyModifierActive(KeyModifier.ALT)
        || Boolean.FALSE.equals(options.requireAlt) && data.isKeyModifierActive(KeyModifier.ALT)) {
        continue;
      }

      this.safeDispatch(eventType, handler, event);
      if (event.stoppedPropagation) {
        break;
      }
    }
  }

  public enum KeyboardEventType {
    KEY_UP,
    KEY_DOWN
  }
}
