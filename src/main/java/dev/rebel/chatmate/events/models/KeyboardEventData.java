package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.KeyboardEventService.KeyboardEventType;
import dev.rebel.chatmate.util.EnumHelpers;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.*;

public class KeyboardEventData {
  public final KeyboardEventType event;

  public final int eventKey;
  public final char eventCharacter;
  public final Map<Integer, Character> currentlyHeldDown;

  public KeyboardEventData(KeyboardEventType event, int eventKey, char eventCharacter, Map<Integer, Character> currentlyHeldDown) {
    this.event = event;
    this.eventKey = eventKey;
    this.eventCharacter = eventCharacter;
    this.currentlyHeldDown = new HashMap<>(currentlyHeldDown);
  }

  public boolean isPressed(int key) {
    return this.event == KeyboardEventType.KEY_DOWN && this.eventKey == key;
  }

  public boolean isReleased(int key) {
    return this.event == KeyboardEventType.KEY_UP && this.eventKey == key;
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
        throw EnumHelpers.<KeyModifier>assertUnreachable(modifier);
    }
  }

  public enum KeyModifier {
    SHIFT,
    CTRL,
    ALT
  }

}
