package dev.rebel.chatmate.services;

import java.util.ArrayList;
import java.util.function.Consumer;

public class EventEmitterService<TPayload> {
  private final ArrayList<Consumer<TPayload>> _listeners;

  public EventEmitterService() {
    this._listeners = new ArrayList<>();
  }

  public final void listen(Consumer<TPayload> callback) {
    this._listeners.add(callback);
  }

  protected final void clear() {
    this._listeners.clear();
  }

  protected final void dispatch(TPayload data) {
    this._listeners.forEach(l -> {
      try {
        l.accept(data);
      } catch (Exception e) {
        System.out.println("[EventEmitterService] Failed to notify listener of new data: " + e.getMessage());
      }
    });
  }
}
