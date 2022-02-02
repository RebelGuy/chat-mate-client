package dev.rebel.chatmate.models;

import dev.rebel.chatmate.services.EventEmitterService;
import dev.rebel.chatmate.services.LoggingService;
import dev.rebel.chatmate.services.util.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Config {
  private final ConfigPersistorService configPersistorService;

  /** Listeners are notified whenever any change has been made to the config. */
  private final List<Callback> updateListeners;
  private final StatefulEmitter<Boolean> apiEnabled;
  public StatefulEmitter<Boolean> getApiEnabled() { return this.apiEnabled; }

  private final StatefulEmitter<Boolean> soundEnabled;
  public StatefulEmitter<Boolean> getSoundEnabled() { return this.soundEnabled; }

  private final StatefulEmitter<Integer> chatVerticalDisplacement;
  public StatefulEmitter<Integer> getChatVerticalDisplacement() { return this.chatVerticalDisplacement; }

  private final StatefulEmitter<Boolean> hudEnabled; // todo: add more hud options, preferably in its own menu
  public StatefulEmitter<Boolean> getHudEnabled() { return this.hudEnabled; }

  public Config(ConfigPersistorService configPersistorService) {
    this.configPersistorService = configPersistorService;

    this.apiEnabled = new StatefulEmitter<>(false, this::onUpdate);
    this.soundEnabled = new StatefulEmitter<>(true, this::onUpdate);
    this.chatVerticalDisplacement = new StatefulEmitter<>(10, this::onUpdate);
    this.hudEnabled = new StatefulEmitter<>(true, this::onUpdate);

    this.updateListeners = new ArrayList<>();

    this.load();
  }

  public void listenAny(Callback callback) {
    this.updateListeners.add(callback);
  }

  private <T> void onUpdate(T _unused) {
    this.save();
    this.updateListeners.forEach(Callback::call);
  }

  private void load() {
    SerialisedConfig loaded = this.configPersistorService.load();
    if (loaded != null) {
      this.soundEnabled.set(loaded.soundEnabled);
      this.chatVerticalDisplacement.set(loaded.chatVerticalDisplacement);
      this.hudEnabled.set(loaded.hudEnabled);
    }
  }

  private void save() {
    SerialisedConfig serialisedConfig = new SerialisedConfig(
      this.soundEnabled.get(),
      this.chatVerticalDisplacement.get(),
      this.hudEnabled.get()
    );
    this.configPersistorService.save(serialisedConfig);
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
