package dev.rebel.chatmate.gui.StateManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Observable<T> {
  private T value;
  private List<Consumer<T>> listeners;

  public Observable(T value) {
    this.value = value;
    this.listeners = new ArrayList<>();
  }

  public void setValue(T value) {
    if (this.value != value) {
      this.value = value;
      this.onChange(value);
    }
  }

  public T getValue() {
    return this.value;
  }

  public void listen(Consumer<T> valueListener) {
    this.listeners.add(valueListener);
  }

  private void onChange(T value) {
    this.listeners.forEach(l -> l.accept(value));
  }
}
