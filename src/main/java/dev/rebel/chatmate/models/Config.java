package dev.rebel.chatmate.models;

import dev.rebel.chatmate.services.EventEmitterService;
import dev.rebel.chatmate.services.util.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Config {
  /** Listeners are notified whenever any change has been made to the config. */
  private final List<Callback> updateListeners;
  public final StatefulEmitter<Boolean> apiEnabled;
  public final StatefulEmitter<Boolean> soundEnabled;
  public final StatefulEmitter<Integer> chatVerticalDisplacement;
  public final StatefulEmitter<Boolean> hudEnabled; // todo: add more hud options, preferrably in its own menu

  public Config() {
    this.apiEnabled = new StatefulEmitter<>(false, this::onUpdate);
    this.soundEnabled = new StatefulEmitter<>(true, this::onUpdate);
    this.chatVerticalDisplacement = new StatefulEmitter<>(15, this::onUpdate);
    this.hudEnabled = new StatefulEmitter<>(true, this::onUpdate);

    this.updateListeners = new ArrayList<>();
  }

  public void listenAny(Callback callback) {
    this.updateListeners.add(callback);
  }

  private <T> void onUpdate(T _unused) {
    this.updateListeners.forEach(Callback::call);
  }

  /** Represents a state that emits an event when modified */
  public static class StatefulEmitter<T> extends EventEmitterService<T> {
    private T state;

    @SafeVarargs
    public StatefulEmitter (T initialState, Consumer<T>... initialListeners) {
      super();
      this.state = initialState;
      Arrays.asList(initialListeners).forEach(super::listen);
    }

    public void set(T newValue) {
      if (this.state == newValue) {
        return;
      } else {
        this.state = newValue;
      }

      super.dispatch(newValue);
    }

    public T get() {
      return this.state;
    }
  }
}
