package dev.rebel.chatmate.services;

import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.Tick;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.function.Supplier;

// Events here will only fire on the initial key-press, NOT when holding down the key.
public class KeyBindingService {
  private final ForgeEventService forgeEventService;

  // do NOT add more of these here - refactor instead of copy-pasting everything!
  private final KeyBinding decrementCounterKeybinding;
  private final KeyBinding incrementCounterKeybinding;

  private final ArrayList<Supplier<Boolean>> decrementCounterListeners;
  private final ArrayList<Supplier<Boolean>> incrementCounterListeners;

  public KeyBindingService(ForgeEventService eventService) {
    this.forgeEventService = eventService;

    this.decrementCounterKeybinding = new KeyBinding("Decrement Counter", Keyboard.KEY_LBRACKET, "ChatMate");
    this.incrementCounterKeybinding = new KeyBinding("Increment Counter", Keyboard.KEY_RBRACKET, "ChatMate");

    this.decrementCounterListeners = new ArrayList<>();
    this.incrementCounterListeners = new ArrayList<>();

    this.registerKeybindings();
    this.registerHandlers();
  }

  public void onDecrementCounterPressed(Supplier<Boolean> callback) {
    this.decrementCounterListeners.add(callback);
  }

  public void onIncrementCounterPressed(Supplier<Boolean> callback) {
    this.incrementCounterListeners.add(callback);
  }

  private void registerKeybindings() {
    ClientRegistry.registerKeyBinding(this.decrementCounterKeybinding);
    ClientRegistry.registerKeyBinding(this.incrementCounterKeybinding);
  }

  private void registerHandlers() {
    this.forgeEventService.onClientTick(this::onClientTick, null);
  }

  private Tick.Out onClientTick(Tick.In eventIn) {
    if (this.decrementCounterKeybinding.isPressed()) {
      this.notifyListeners(this.decrementCounterListeners);
    }

    if (this.incrementCounterKeybinding.isPressed()) {
      this.notifyListeners(this.incrementCounterListeners);
    }

    return null;
  }

  private void notifyListeners(ArrayList<Supplier<Boolean>> listeners) {
    for (Supplier<Boolean> listener : listeners) {
      if (listener.get()) {
        return;
      }
    }
  }
}
