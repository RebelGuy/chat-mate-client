package dev.rebel.chatmate.models;

import dev.rebel.chatmate.services.EventEmitterService;

public class Config {
  public final StatefulEmitter<Boolean> apiEnabled;
  public final StatefulEmitter<Boolean> soundEnabled;


  public Config() {
    this.apiEnabled = new StatefulEmitter<>(false);
    this.soundEnabled = new StatefulEmitter<>(true);
  }

  public static class StatefulEmitter<T> extends EventEmitterService<T> {
    private T state;

    public StatefulEmitter (T initialState) {
      this.state = initialState;
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
