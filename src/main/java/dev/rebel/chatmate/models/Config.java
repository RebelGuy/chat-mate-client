package dev.rebel.chatmate.models;

import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV0;
import dev.rebel.chatmate.services.EventEmitterService;
import dev.rebel.chatmate.services.util.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Config {
  // Since config settings are stored locally, whenever you make changes to the Config that affect the serialised model,
  // you must also change the schema. This is an additive process, and existing serialised models must not be changed.
  // 1. Create new model version that extends `Version`
  // 2. Update the generic type of ConfigPersistorService
  // 3. Bump the ConfigPersistorService.CURRENT_VERSION
  // 4. Add a new migration file
  // 5. Use the new migration file when loading the serialised config
  private final ConfigPersistorService<SerialisedConfigV0> configPersistorService;

  /** Listeners are notified whenever any change has been made to the config. */
  private final List<Callback> updateListeners;
  private final StatefulEmitter<Boolean> chatMateEnabled;
  public StatefulEmitter<Boolean> getChatMateEnabledEmitter() { return this.chatMateEnabled; }

  private final StatefulEmitter<Boolean> soundEnabled;
  public StatefulEmitter<Boolean> getSoundEnabledEmitter() { return this.soundEnabled; }

  private final StatefulEmitter<Integer> chatVerticalDisplacement;
  public StatefulEmitter<Integer> getChatVerticalDisplacementEmitter() { return this.chatVerticalDisplacement; }

  private final StatefulEmitter<Boolean> hudEnabled; // todo: add more hud options, preferably in its own menu
  public StatefulEmitter<Boolean> getHudEnabledEmitter() { return this.hudEnabled; }

  private final StatefulEmitter<Boolean> showStatusIndicator;
  public StatefulEmitter<Boolean> getShowStatusIndicatorEmitter() { return this.showStatusIndicator; }

  private final StatefulEmitter<Boolean> showLiveViewers;
  public StatefulEmitter<Boolean> getShowLiveViewersEmitter() { return this.showLiveViewers; }

  public Config(ConfigPersistorService configPersistorService) {
    this.configPersistorService = configPersistorService;

    this.chatMateEnabled = new StatefulEmitter<>(false, this::onUpdate);
    this.soundEnabled = new StatefulEmitter<>(true, this::onUpdate);
    this.chatVerticalDisplacement = new StatefulEmitter<>(10, this::onUpdate);
    this.hudEnabled = new StatefulEmitter<>(true, this::onUpdate);
    this.showStatusIndicator = new StatefulEmitter<>(true, this::onUpdate);
    this.showLiveViewers = new StatefulEmitter<>(true, this::onUpdate);

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
    SerialisedConfigV0 loaded = this.configPersistorService.load();
    if (loaded != null) {
      this.soundEnabled.set(loaded.soundEnabled);
      this.chatVerticalDisplacement.set(loaded.chatVerticalDisplacement);
      this.hudEnabled.set(loaded.hudEnabled);
      this.showStatusIndicator.set(loaded.showStatusIndicator);
      this.showLiveViewers.set(loaded.showLiveViewers);
    }
  }

  private void save() {
    SerialisedConfigV0 serialisedConfig = new SerialisedConfigV0(
      this.soundEnabled.get(),
      this.chatVerticalDisplacement.get(),
      this.hudEnabled.get(),
      this.showStatusIndicator.get(),
      this.showLiveViewers.get()
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
