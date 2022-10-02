package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.models.ConfigEventData.In;
import dev.rebel.chatmate.events.models.ConfigEventData.Out;
import dev.rebel.chatmate.events.models.ConfigEventData.Options;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class ConfigEventData<T> extends EventData<In<T>, Out<T>, Options<T>> {
  public static class In<T> extends EventIn {
    public final T data;

    public In(T data) {
      this.data = data;
    }
  }

  public static class Out<T> extends EventOut {
    public Out() { }
  }

  public static class Options<T> extends EventOptions {
    public final @Nullable Predicate<T> filter;

    /** Fire the event when the config value has changed. */
    public Options() {
      this.filter = null;
    }

    /** Fire the event when the config value has changed to the specified value. Uses `.equals()` for the equality check. */
    public Options(@Nullable T listenForValue) {
      this.filter = v -> v == null && listenForValue == null || v != null && v.equals(listenForValue);
    }

    /** Fire the event when the config value satisfies the provided predicate. */
    public Options(Predicate<T> listenForPredicate) {
      this.filter = listenForPredicate;
    }
  }
}
