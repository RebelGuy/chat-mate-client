package dev.rebel.chatmate.services;

import dev.rebel.chatmate.events.ForgeEventService;
import dev.rebel.chatmate.events.models.Tick;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Events here will only fire on the initial key-press, NOT when holding down the key. */
public class KeyBindingService {
  private final ForgeEventService forgeEventService;

  private final Map<ChatMateKeyEvent, KeyBinding> keyBindings;
  private final Map<ChatMateKeyEvent, ArrayList<Supplier<Boolean>>> listeners;

  public KeyBindingService(ForgeEventService eventService) {
    this.forgeEventService = eventService;

    this.keyBindings = new HashMap<>();
    this.keyBindings.put(ChatMateKeyEvent.DECREMENT_COUNTER, new KeyBinding("Decrement Counter", Keyboard.KEY_LBRACKET, "ChatMate"));
    this.keyBindings.put(ChatMateKeyEvent.INCREMENT_COUNTER, new KeyBinding("Increment Counter", Keyboard.KEY_RBRACKET, "ChatMate"));
    this.keyBindings.put(ChatMateKeyEvent.OPEN_CHAT_MATE_HUD, new KeyBinding("Open ChatMate HUD", Keyboard.KEY_Y, "ChatMate"));

    this.listeners = new HashMap<>();
    for (ChatMateKeyEvent event : ChatMateKeyEvent.values()) {
      this.listeners.put(event, new ArrayList<>());
    }

    this.verifyInitialisation();
    this.registerKeybindings();
    this.registerHandlers();
  }

  /** Listen to a keypress event. Return `true` to prevent the event from propagating further.
   * Listeners are called in the same order in which they registered.
   */
  public void on(ChatMateKeyEvent event, Supplier<Boolean> callback) {
    this.listeners.get(event).add(callback);
  }

  private void registerKeybindings() {
    for (ChatMateKeyEvent event : ChatMateKeyEvent.values()) {
      ClientRegistry.registerKeyBinding(this.keyBindings.get(event));
    }
  }

  private void registerHandlers() {
    this.forgeEventService.onClientTick(this::onClientTick, null);
  }

  private Tick.Out onClientTick(Tick.In eventIn) {
    for (ChatMateKeyEvent event : ChatMateKeyEvent.values()) {
      KeyBinding keyBinding = this.keyBindings.get(event);

      if (keyBinding.isPressed()) {
        ArrayList<Supplier<Boolean>> listeners = this.listeners.get(event);
        this.notifyListeners(listeners);
      }
    }

    return new Tick.Out();
  }

  private void notifyListeners(ArrayList<Supplier<Boolean>> listeners) {
    for (Supplier<Boolean> listener : listeners) {
      if (listener.get()) {
        return;
      }
    }
  }

  private void verifyInitialisation() {
    for (ChatMateKeyEvent event : ChatMateKeyEvent.values()) {
      if (this.keyBindings.get(event) == null) {
        throw new RuntimeException("No KeyBinding has been registered for event " + event.name());
      }
      if (this.listeners.get(event) == null) {
        throw new RuntimeException("Listener list has not been initialised for event " + event.name());
      }
    }
  }

  public enum ChatMateKeyEvent {
    INCREMENT_COUNTER,
    DECREMENT_COUNTER,
    OPEN_CHAT_MATE_HUD
  }
}
