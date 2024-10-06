package dev.rebel.chatmate.events.models;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class ConfigEventOptions<T> {
  public final @Nullable Predicate<T> filter;

  /** Fire the event when the config value has changed. */
  public ConfigEventOptions() {
    this.filter = null;
  }

  /** Fire the event when the config value has changed to the specified value. Uses `.equals()` for the equality check. */
  public ConfigEventOptions(@Nullable T listenForValue) {
    this.filter = v -> v == null && listenForValue == null || v != null && v.equals(listenForValue);
  }

  /** Fire the event when the config value satisfies the provided predicate. */
  public ConfigEventOptions(Predicate<T> listenForPredicate) {
    this.filter = listenForPredicate;
  }
}
