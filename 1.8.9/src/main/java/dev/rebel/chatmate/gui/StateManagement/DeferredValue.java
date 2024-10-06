package dev.rebel.chatmate.gui.StateManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Notifies listeners when a value has been set. Every listener will only fire once with the current/first value. */
public class DeferredValue<T> {
  private boolean isSet = false;
  private T value = null;
  private final List<Consumer<T>> listeners = new ArrayList<>();

  public DeferredValue<T> setValue(T value) {
    this.value = value;

    if (!this.isSet) {
      this.isSet = true;
      this.listeners.forEach(this::notifyListener);
    }

    return this;
  }

  public void getValue(Consumer<T> listener) {
    if (this.isSet) {
      this.notifyListener(listener);
    } else {
      this.listeners.add(listener);
    }
  }

  private void notifyListener(Consumer<T> listener) {
    try {
      listener.accept(this.value);
    } catch (Exception ignored) { }
  }
}
