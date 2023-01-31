package dev.rebel.chatmate.config;

import dev.rebel.chatmate.config.Config.ConfigType;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV5;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV6;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudElement;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.EventHandler;
import dev.rebel.chatmate.events.EventServiceBase;
import dev.rebel.chatmate.events.models.ConfigEventData;
import dev.rebel.chatmate.events.models.ConfigEventData.In;
import dev.rebel.chatmate.events.models.ConfigEventData.Options;
import dev.rebel.chatmate.events.models.ConfigEventData.Out;
import dev.rebel.chatmate.events.models.EventData.EventIn;
import dev.rebel.chatmate.events.models.EventData.EventOut;
import dev.rebel.chatmate.util.Callback;
import dev.rebel.chatmate.util.Debouncer;
import dev.rebel.chatmate.util.EnumHelpers;

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
  private final ConfigPersistorService<SerialisedConfigV6> configPersistorService;
  private final DimFactory dimFactory;

  // NOTE: DO **NOT** REMOVE THESE GETTERS. THEY ARE DISGUSTING BUT REQUIRED FOR TESTING
  private final StatefulEmitter<Boolean> chatMateEnabled;
  public StatefulEmitter<Boolean> getChatMateEnabledEmitter() { return this.chatMateEnabled; }

  private final StatefulEmitter<Boolean> soundEnabled;
  public StatefulEmitter<Boolean> getSoundEnabledEmitter() { return this.soundEnabled; }

  private final StatefulEmitter<Integer> chatVerticalDisplacement;
  public StatefulEmitter<Integer> getChatVerticalDisplacementEmitter() { return this.chatVerticalDisplacement; }

  private final StatefulEmitter<CommandMessageChatVisibility> commandMessageChatVisibility;
  public StatefulEmitter<CommandMessageChatVisibility> getCommandMessageChatVisibilityEmitter() { return this.commandMessageChatVisibility; }

  private final StatefulEmitter<Boolean> hudEnabled;
  public StatefulEmitter<Boolean> getHudEnabledEmitter() { return this.hudEnabled; }

  private final StatefulEmitter<Boolean> showChatPlatformIcon;
  public StatefulEmitter<Boolean> getShowChatPlatformIconEmitter() { return this.showChatPlatformIcon; }

  private final StatefulEmitter<SeparableHudElement> statusIndicator;
  public StatefulEmitter<SeparableHudElement> getStatusIndicatorEmitter() { return this.statusIndicator; }

  private final StatefulEmitter<SeparableHudElement> viewerCount;
  public StatefulEmitter<SeparableHudElement> getViewerCountEmitter() { return this.viewerCount; }

  private final StatefulEmitter<Boolean> debugModeEnabled;
  public StatefulEmitter<Boolean> getDebugModeEnabledEmitter() { return this.debugModeEnabled; }

  private final StatefulEmitter<Long> lastGetChatResponse;
  public StatefulEmitter<Long> getLastGetChatResponseEmitter() { return this.lastGetChatResponse; }

  private final StatefulEmitter<Long> lastGetChatMateEventsResponse;
  public StatefulEmitter<Long> getLastGetChatMateEventsResponseEmitter() { return this.lastGetChatMateEventsResponse; }

  private final StatefulEmitter<Map<String, HudElementTransform>> hudTransforms;
  public StatefulEmitter<Map<String, HudElementTransform>> getHudTransformsEmitter() { return this.hudTransforms; }

  private final StatefulEmitter<LoginInfo> loginInfo;
  public StatefulEmitter<LoginInfo> getLoginInfoEmitter() { return this.loginInfo; }

  /** Listeners are notified whenever any change has been made to the config. */
  private final List<Callback> updateListeners;

  /** Only used for holding onto wrapped callback functions when an onChange subscriber uses the automatic unsubscription feature. Write-only. */
  private final Map<ConfigType, WeakHashMap<Object, Function<? extends EventIn, ? extends EventOut>>> weakHandlers;

  private final Debouncer saveDebouncer;

  public Config(LogService logService, ConfigPersistorService<SerialisedConfigV6> configPersistorService, DimFactory dimFactory) {
    super(ConfigType.class, logService);
    this.configPersistorService = configPersistorService;
    this.dimFactory = dimFactory;
    this.saveDebouncer = new Debouncer(400, this::save); // has to be less than the API polling rate
    this.updateListeners = new ArrayList<>();

    this.weakHandlers = new WeakHashMap<>();
    for (ConfigType type : ConfigType.class.getEnumConstants()) {
      this.weakHandlers.put(type, new WeakHashMap<>());
    }

    // if loading fails, the user will be left with the default settings which is fine
    @Nullable SerialisedConfigV6 data = this.load();
    this.chatMateEnabled = new StatefulEmitter<>(ConfigType.ENABLE_CHAT_MATE, false, this::onUpdate);
    this.soundEnabled = new StatefulEmitter<>(ConfigType.ENABLE_SOUND, data == null ? true : data.soundEnabled, this::onUpdate);
    this.chatVerticalDisplacement = new StatefulEmitter<>(ConfigType.CHAT_VERTICAL_DISPLACEMENT, data == null ? 10 : data.chatVerticalDisplacement, this::onUpdate);
    this.commandMessageChatVisibility = new StatefulEmitter<>(ConfigType.COMMAND_MESSAGE_CHAT_VISIBILITY, data == null || data.commandMessageChatVisibility == null ? CommandMessageChatVisibility.SHOWN : EnumHelpers.fromStringOrDefault(CommandMessageChatVisibility.class, data.commandMessageChatVisibility, CommandMessageChatVisibility.SHOWN), this::onUpdate);
    this.hudEnabled = new StatefulEmitter<>(ConfigType.ENABLE_HUD, data == null ? true : data.hudEnabled, this::onUpdate);
    this.showChatPlatformIcon = new StatefulEmitter<>(ConfigType.SHOW_CHAT_PLATFORM_ICON, data == null ? true : data.showChatPlatformIcon, this::onUpdate);
    this.statusIndicator = new StatefulEmitter<>(ConfigType.STATUS_INDICATOR, data == null ? new SeparableHudElement(true, false, false, SeparableHudElement.PlatformIconPosition.LEFT) : data.statusIndicator.deserialise(), this::onUpdate);
    this.viewerCount = new StatefulEmitter<>(ConfigType.VIEWER_COUNT, data == null ? new SeparableHudElement(true, false, false, SeparableHudElement.PlatformIconPosition.LEFT) : data.viewerCount.deserialise(), this::onUpdate);
    this.debugModeEnabled = new StatefulEmitter<>(ConfigType.DEBUG_MODE_ENABLED, data == null ? false : data.debugModeEnabled, this::onUpdate);
    this.lastGetChatResponse = new StatefulEmitter<>(ConfigType.LAST_GET_CHAT_RESPONSE, data == null ? 0L : data.lastGetChatResponse, this::onUpdate);
    this.lastGetChatMateEventsResponse = new StatefulEmitter<>(ConfigType.LAST_GET_CHAT_MATE_EVENTS_RESPONSE, data == null ? 0L : data.lastGetChatMateEventsResponse, this::onUpdate);
    this.hudTransforms = new StatefulEmitter<>(ConfigType.HUD_TRANSFORMS, data == null ? new HashMap<>() : dev.rebel.chatmate.util.Collections.map(data.hudTransforms, s -> s.deserialise(dimFactory)), this::onUpdate);
    this.loginInfo = new StatefulEmitter<>(ConfigType.LOGIN_INFO, data == null ? new LoginInfo(null, null) : data.loginInfo.deserialise(), this::onUpdate);
  }

  public void listenAny(Callback callback) {
    this.updateListeners.add(callback);
  }

  private <T> Out<T> onUpdate(In<T> _unused) {
    this.saveDebouncer.doDebounce();
    this.updateListeners.forEach(Callback::call);
    return new Out<>();
  }

  @Nullable
  private SerialisedConfigV6 load() {
    SerialisedConfigV6 loaded = this.configPersistorService.load();
    if (loaded != null) {
      this.saveDebouncer.doDebounce();
    }
    return loaded;
  }

  private void save() {
    try {
      SerialisedConfigV6 serialisedConfig = new SerialisedConfigV6(
          this.soundEnabled.get(),
          this.chatVerticalDisplacement.get(),
          this.commandMessageChatVisibility.get().toString(),
          this.hudEnabled.get(),
          this.showChatPlatformIcon.get(),
          new SerialisedConfigV6.SerialisedSeparableHudElement(this.statusIndicator.get()),
          new SerialisedConfigV6.SerialisedSeparableHudElement(this.viewerCount.get()),
          this.debugModeEnabled.get(),
          this.lastGetChatResponse.get(),
          this.lastGetChatMateEventsResponse.get(),
          dev.rebel.chatmate.util.Collections.map(this.hudTransforms.get(), SerialisedConfigV6.SerialisedHudElementTransform::new), // no import aliasing in Java..
          new SerialisedConfigV6.SerialisedLoginInfo(this.loginInfo.get())
      );
      this.configPersistorService.save(serialisedConfig);
    } catch (Exception e) {
      this.logService.logError(this, "Failed to save config:", e);
    }
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

    /** Lambda allowed - no automatic unsubscribing. */
    public void onChange(Consumer<T> callback) {
      Function<In<T>, Out<T>> handler = in -> { callback.accept(in.data); return new Out<>(); };
      this.onChange(handler, null);
    }

    /** **NO LAMBDA** - automatic unsubscribing. */
    public void onChange(Function<In<T>, Out<T>> handler, Object key) {
      this.onChange(handler, key, false);
    }

    // if you try to add a lambda it won't stay subscribed - create a field first which may be a lambda
    /** **NO LAMBDA** - automatic unsubscribing. */
    public void onChange(Function<In<T>, Out<T>> handler, Object key, boolean fireInitialOnChange) {
      this.onChange(handler, new Options<>(), key, fireInitialOnChange);
    }

    /** Lambda allowed - no automatic unsubscribing. */
    public void onChange(Consumer<T> callback, @Nullable Options<T> options) {
      Function<In<T>, Out<T>> handler = in -> { callback.accept(in.data); return new Out<>(); };
      this.onChange(handler, options, null, false);
    }

    /** **NO LAMBDA** - automatic unsubscribing. */
    public void onChange(Function<In<T>, Out<T>> handler, @Nullable Options<T> options, Object key, boolean fireInitialOnChange) {
      Config.this.addListener(this.type, handler, options, key);

      // fireInitialOnChange is a convenience option for callers that perform logic directly inside the callback function - don't remove it
      if (fireInitialOnChange) {
        if (options != null && options.filter != null && !options.filter.test(this.state)) {
          return;
        }
        In<T> eventIn = new In<>(this.state);
        try {
          handler.apply(eventIn);
        } catch (Exception e) {
          Config.this.logService.logError(this, "A problem occurred while notifying listener of the", this.type, "event. Event data:", eventIn, "| Error:", e);
        }
      }
    }

    public void off(Object key) {
      Config.this.removeListener(this.type, key);
    }

    public void set(T newValue) {
      if (Objects.equals(this.state, newValue)) {
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

    public void set(Function<T, T> newValue) {
      this.set(newValue.apply(this.state));
    }

    public T get() {
      return this.state;
    }
  }

  public static class SeparableHudElement {
    public final boolean enabled;
    public final boolean separatePlatforms;
    public final boolean showPlatformIcon;
    public final PlatformIconPosition platformIconPosition;

    public SeparableHudElement(boolean enabled, boolean separatePlatforms, boolean showPlatformIcon, PlatformIconPosition platformIconPosition) {
      this.enabled = enabled;
      this.separatePlatforms = separatePlatforms;
      this.showPlatformIcon = showPlatformIcon;
      this.platformIconPosition = platformIconPosition;
    }

    public SeparableHudElement withEnabled(boolean enabled) {
      return new SeparableHudElement(enabled, this.separatePlatforms, this.showPlatformIcon, this.platformIconPosition);
    }

    public SeparableHudElement withSeparatePlatforms(boolean separatePlatforms) {
      return new SeparableHudElement(this.enabled, separatePlatforms, this.showPlatformIcon, this.platformIconPosition);
    }

    public SeparableHudElement withShowPlatformIcon(boolean showPlatformIcon) {
      return new SeparableHudElement(this.enabled, this.separatePlatforms, showPlatformIcon, this.platformIconPosition);
    }

    public SeparableHudElement withPlatformIconPosition(PlatformIconPosition platformIconPosition) {
      return new SeparableHudElement(this.enabled, this.separatePlatforms, this.showPlatformIcon, platformIconPosition);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SeparableHudElement that = (SeparableHudElement) o;
      return enabled == that.enabled && separatePlatforms == that.separatePlatforms && showPlatformIcon == that.showPlatformIcon && platformIconPosition == that.platformIconPosition;
    }

    @Override
    public int hashCode() {
      return Objects.hash(enabled, separatePlatforms, showPlatformIcon, platformIconPosition);
    }

    public enum PlatformIconPosition { LEFT, RIGHT, TOP, BOTTOM }
  }

  // The (x, y) position is anchored to the current screen, it is not necessarily an absolute position.
  // By holding on to the screen dimensions, we will be able to translate the position from the current screen
  // context to another screen context in a natural way (and without the position ever going off screen).
  public static class HudElementTransform {
    public final Dim x;
    public final Dim y;
    public final HudElement.Anchor positionAnchor;
    public final DimRect screenRect;
    public final int screenScaleFactor;
    public final float scale;

    public HudElementTransform(Dim x, Dim y, HudElement.Anchor positionAnchor, DimRect screenRect, int screenScaleFactor, float scale) {
      this.x = x;
      this.y = y;
      this.positionAnchor = positionAnchor;
      this.screenRect = screenRect;
      this.screenScaleFactor = screenScaleFactor;
      this.scale = scale;
    }

    public DimPoint getPosition() {
      return new DimPoint(this.x, this.y);
    }
  }

  public static class LoginInfo {
    public final @Nullable String username;
    public final @Nullable String loginToken;

    public LoginInfo(@Nullable String username, @Nullable String loginToken) {
      this.username = username;
      this.loginToken = loginToken;
    }
  }

  public enum CommandMessageChatVisibility { HIDDEN, SHOWN, GREYED_OUT }

  public enum ConfigType {
    ENABLE_CHAT_MATE,
    ENABLE_SOUND,
    CHAT_VERTICAL_DISPLACEMENT,
    COMMAND_MESSAGE_CHAT_VISIBILITY,
    ENABLE_HUD,
    SHOW_SERVER_LOGS_HEARTBEAT,
    SHOW_SERVER_LOGS_TIME_SERIES,
    SHOW_CHAT_PLATFORM_ICON,
    STATUS_INDICATOR,
    VIEWER_COUNT,
    DEBUG_MODE_ENABLED,
    LAST_GET_CHAT_RESPONSE,
    LAST_GET_CHAT_MATE_EVENTS_RESPONSE,
    HUD_TRANSFORMS,
    LOGIN_INFO
  }
}
