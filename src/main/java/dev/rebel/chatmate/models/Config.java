package dev.rebel.chatmate.models;

import dev.rebel.chatmate.models.Config.ConfigType;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV2;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.EventHandler;
import dev.rebel.chatmate.services.events.EventServiceBase;
import dev.rebel.chatmate.services.events.models.ConfigEventData;
import dev.rebel.chatmate.services.events.models.ConfigEventData.In;
import dev.rebel.chatmate.services.events.models.ConfigEventData.Options;
import dev.rebel.chatmate.services.events.models.ConfigEventData.Out;
import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;
import dev.rebel.chatmate.services.util.Callback;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Config extends EventServiceBase<ConfigType> {
  // Since config settings are stored locally, whenever you make changes to the Config that affect the serialised model,
  // you must also change the schema. This is an additive process, and existing serialised models must not be changed.
  // 1. Create new model version that extends `Version` in `SerialisedConfigVersions.java`
  // 2. Update the generic type of ConfigPersistorService set in ChatMate.java, and in here
  // 3. Bump the ConfigPersistorService.CURRENT_VERSION
  // 4. Add a new migration file in the `configMigrations` package
  // 5. Add the migration class from the previous step and the new serialised model to the arrays in `Migration.java`
  // 6. Change the type of the saved and loaded serialised config in this file
  private final ConfigPersistorService<SerialisedConfigV2> configPersistorService;

  private final StatefulEmitter<Boolean> chatMateEnabled;
  public StatefulEmitter<Boolean> getChatMateEnabledEmitter() { return this.chatMateEnabled; }

  private final StatefulEmitter<Boolean> soundEnabled;
  public StatefulEmitter<Boolean> getSoundEnabledEmitter() { return this.soundEnabled; }

  private final StatefulEmitter<Integer> chatVerticalDisplacement;
  public StatefulEmitter<Integer> getChatVerticalDisplacementEmitter() { return this.chatVerticalDisplacement; }

  private final StatefulEmitter<Boolean> hudEnabled;
  public StatefulEmitter<Boolean> getHudEnabledEmitter() { return this.hudEnabled; }

  private final StatefulEmitter<Boolean> showStatusIndicator;
  public StatefulEmitter<Boolean> getShowStatusIndicatorEmitter() { return this.showStatusIndicator; }

  private final StatefulEmitter<Boolean> showLiveViewers;
  public StatefulEmitter<Boolean> getShowLiveViewersEmitter() { return this.showLiveViewers; }

  private final StatefulEmitter<Boolean> showServerLogsHeartbeat;
  public StatefulEmitter<Boolean> getShowServerLogsHeartbeat() { return this.showServerLogsHeartbeat; }

  private final StatefulEmitter<Boolean> showServerLogsTimeSeries;
  public StatefulEmitter<Boolean> getShowServerLogsTimeSeries() { return this.showServerLogsTimeSeries; }

  private final StatefulEmitter<Boolean> separatePlatforms;
  public StatefulEmitter<Boolean> getSeparatePlatforms() { return this.separatePlatforms; }

  /** Listeners are notified whenever any change has been made to the config. */
  private final List<Callback> updateListeners;

  /** Only used for holding onto wrapped callback functions when an onChange subscriber uses the automatic unsubscription feature. Write-only. */
  private final Map<ConfigType, WeakHashMap<Object, Function<? extends EventIn, ? extends EventOut>>> weakHandlers;

  public Config(LogService logService, ConfigPersistorService configPersistorService) {
    super(ConfigType.class, logService);
    this.configPersistorService = configPersistorService;

    this.updateListeners = new ArrayList<>();
    this.chatMateEnabled = new StatefulEmitter<>(ConfigType.ENABLE_CHAT_MATE, false, this::onUpdate);
    this.soundEnabled = new StatefulEmitter<>(ConfigType.ENABLE_SOUND, true, this::onUpdate);
    this.chatVerticalDisplacement = new StatefulEmitter<>(ConfigType.CHAT_VERTICAL_DISPLACEMENT, 10, this::onUpdate);
    this.hudEnabled = new StatefulEmitter<>(ConfigType.ENABLE_HUD, true, this::onUpdate);
    this.showStatusIndicator = new StatefulEmitter<>(ConfigType.SHOW_STATUS_INDICATOR, true, this::onUpdate);
    this.showLiveViewers = new StatefulEmitter<>(ConfigType.SHOW_LIVE_VIEWERS, true, this::onUpdate);
    this.showServerLogsHeartbeat = new StatefulEmitter<>(ConfigType.SHOW_SERVER_LOGS_HEARTBEAT, true, this::onUpdate);
    this.showServerLogsTimeSeries = new StatefulEmitter<>(ConfigType.SHOW_SERVER_LOGS_TIME_SERIES, false, this::onUpdate);
    this.separatePlatforms = new StatefulEmitter<>(ConfigType.IDENTIFY_PLATFORMS, false, this::onUpdate);

    this.weakHandlers = new WeakHashMap<>();
    for (ConfigType type : ConfigType.class.getEnumConstants()) {
      this.weakHandlers.put(type, new WeakHashMap<>());
    }
    this.load();
  }

  public void listenAny(Callback callback) {
    this.updateListeners.add(callback);
  }

  private <T> Out<T> onUpdate(In<T> _unused) {
    this.save();
    this.updateListeners.forEach(Callback::call);
    return new Out<>();
  }

  private void load() {
    SerialisedConfigV2 loaded = this.configPersistorService.load();
    if (loaded != null) {
      this.soundEnabled.set(loaded.soundEnabled);
      this.chatVerticalDisplacement.set(loaded.chatVerticalDisplacement);
      this.hudEnabled.set(loaded.hudEnabled);
      this.showStatusIndicator.set(loaded.showStatusIndicator);
      this.showLiveViewers.set(loaded.showLiveViewers);
      this.showServerLogsHeartbeat.set(loaded.showServerLogsHeartbeat);
      this.showServerLogsTimeSeries.set(loaded.showServerLogsTimeSeries);
      this.separatePlatforms.set(loaded.identifyPlatforms);
      this.save();
    }
  }

  private void save() {
    SerialisedConfigV2 serialisedConfig = new SerialisedConfigV2(
      this.soundEnabled.get(),
      this.chatVerticalDisplacement.get(),
      this.hudEnabled.get(),
      this.showStatusIndicator.get(),
      this.showLiveViewers.get(),
      this.showServerLogsHeartbeat.get(),
      this.showServerLogsTimeSeries.get(),
      this.separatePlatforms.get()
    );
    this.configPersistorService.save(serialisedConfig);
  }

  /** Represents a state that emits an event when modified */
  public class StatefulEmitter<T> {
    private final ConfigType type;
    private T state;

    @SafeVarargs
    public StatefulEmitter (ConfigType type, T initialState, Function<In<T>, Out<T>>... initialListeners) {
      super();
      this.type = type;
      this.state = initialState;
      Arrays.asList(initialListeners).forEach(l -> Config.this.addListener(this.type, l, null));
    }

    /** Lambda allowed. */
    public void onChange(Consumer<T> callback) {
      this.onChange(callback, null);
    }

    /** **NO LAMBDA** */
    public void onChange(Consumer<T> callback, Object key) {
      this.onChange(callback, new Options<>(), key);
    }

    /** Lambda allowed. */
    public void onChange(Consumer<T> callback, @Nullable Options<T> options) {
      this.onChange(callback, options, null);
    }

    /** **NO LAMBDA** */
    public void onChange(Consumer<T> callback, @Nullable Options<T> options, Object key) {
      // must hold on to a reference of the transformed callback
      Function<In<T>, Out<T>> handler = in -> { callback.accept(in.data); return new Out<>(); };
      Config.this.weakHandlers.get(this.type).put(key, handler);
      Config.this.addListener(this.type, handler, options, key);
    }

    public void off(Object key) {
      Config.this.removeListener(this.type, key);
    }

    public void set(T newValue) {
      if (this.state == null && newValue == null || this.state != null && this.state.equals(newValue)) {
        return;
      } else {
        this.state = newValue;
      }

      ArrayList<EventHandler<In<T>, Out<T>, Options<T>>> handlers = Config.this.getListeners(this.type, ConfigEventData.class);
      for (EventHandler<In<T>, Out<T>, Options<T>> handler : handlers) {
        Options<T> options = handler.options;
        if (options != null && options.filter != null && !options.filter.test(newValue)) {
          continue;
        }

        In<T> eventIn = new In<>(newValue);
        Out<T> eventOut = Config.this.safeDispatch(this.type, handler, eventIn);
      }
    }

    public T get() {
      return this.state;
    }
  }

  public enum ConfigType {
    ENABLE_CHAT_MATE,
    ENABLE_SOUND,
    CHAT_VERTICAL_DISPLACEMENT,
    ENABLE_HUD,
    SHOW_STATUS_INDICATOR,
    SHOW_LIVE_VIEWERS,
    SHOW_SERVER_LOGS_HEARTBEAT,
    SHOW_SERVER_LOGS_TIME_SERIES,
    IDENTIFY_PLATFORMS
  }
}
