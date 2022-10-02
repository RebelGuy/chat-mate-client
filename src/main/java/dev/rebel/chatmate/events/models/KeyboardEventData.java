package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.KeyboardEventService.Events;
import dev.rebel.chatmate.events.models.KeyboardEventData.In;
import dev.rebel.chatmate.events.models.KeyboardEventData.Out;
import dev.rebel.chatmate.events.models.KeyboardEventData.Options;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.*;

public class KeyboardEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final Events event;

    public final int eventKey;
    public final char eventCharacter;
    public final Map<Integer, Character> currentlyHeldDown;

    public In(Events event, int eventKey, char eventCharacter, Map<Integer, Character> currentlyHeldDown) {
      this.event = event;
      this.eventKey = eventKey;
      this.eventCharacter = eventCharacter;
      this.currentlyHeldDown = new HashMap<>(currentlyHeldDown);
    }

    public boolean isPressed(int key) {
      return this.event == Events.KEY_DOWN && this.eventKey == key;
    }

    public boolean isReleased(int key) {
      return this.event == Events.KEY_UP && this.eventKey == key;
    }

    public boolean isKeyModifierActive(KeyModifier modifier) {
      Set<Integer> keys = this.currentlyHeldDown.keySet();
      switch (modifier) {
        case SHIFT:
          return keys.contains(Keyboard.KEY_LSHIFT) || keys.contains(Keyboard.KEY_RSHIFT);
        case CTRL:
          return keys.contains(Keyboard.KEY_LCONTROL) || keys.contains(Keyboard.KEY_RCONTROL);
        case ALT:
          return keys.contains(Keyboard.KEY_LMENU) || keys.contains(Keyboard.KEY_RMENU);
        default:
          throw new RuntimeException("Did not expect to get here");
      }
    }

    public enum KeyModifier {
      SHIFT,
      CTRL,
      ALT
    }
  }

  public static class Out extends EventOut {
    public final @Nullable KeyboardHandlerAction handlerAction;

    public Out() {
      this(null);
    }

    public Out(@Nullable KeyboardHandlerAction action) {
      this.handlerAction = action;
    }

    public enum KeyboardHandlerAction {
      /** The keyboard action was handled, but allow the event to continue propagation. */
      HANDLED,

      /** The keyboard action was handled, and the event should immediately stop propagating. */
      SWALLOWED
    }
  }

  public static class Options extends EventOptions {
    public final boolean ignoreHandled;
    public final @Nullable Boolean requireShift;
    public final @Nullable Boolean requireCtrl;
    public final @Nullable Boolean requireAlt;
    public final @Nullable Set<Integer> listenForKeys;

    /** Listen to all keyboard events. */
    public Options() {
      this(false, null, null, null);
    }

    public Options(boolean ignoreHandled, @Nullable Boolean requireShift, @Nullable Boolean requireCtrl, @Nullable Boolean requireAlt) {
      this.ignoreHandled = ignoreHandled;
      this.requireShift = requireShift;
      this.requireCtrl = requireCtrl;
      this.requireAlt = requireAlt;
      this.listenForKeys = null;
    }

    /** Only fire keyboard events for the specified key types. */
    public Options(boolean ignoreHandled, @Nullable Boolean requireShift, @Nullable Boolean requireCtrl, @Nullable Boolean requireAlt, Integer... listenForKeys) {
      this.ignoreHandled = ignoreHandled;
      this.requireShift = requireShift;
      this.requireCtrl = requireCtrl;
      this.requireAlt = requireAlt;
      this.listenForKeys = new HashSet<>(Arrays.asList(listenForKeys));
    }
  }
}
