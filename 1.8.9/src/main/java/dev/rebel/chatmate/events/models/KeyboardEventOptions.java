package dev.rebel.chatmate.events.models;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeyboardEventOptions {
  public final @Nullable Boolean requireShift;
  public final @Nullable Boolean requireCtrl;
  public final @Nullable Boolean requireAlt;
  public final @Nullable Set<Integer> listenForKeys;

  /** Listen to all keyboard events. */
  public KeyboardEventOptions() {
    this(null, null, null);
  }

  public KeyboardEventOptions(@Nullable Boolean requireShift, @Nullable Boolean requireCtrl, @Nullable Boolean requireAlt) {
    this.requireShift = requireShift;
    this.requireCtrl = requireCtrl;
    this.requireAlt = requireAlt;
    this.listenForKeys = null;
  }

  /** Only fire keyboard events for the specified key types. */
  public KeyboardEventOptions(@Nullable Boolean requireShift, @Nullable Boolean requireCtrl, @Nullable Boolean requireAlt, Integer... listenForKeys) {
    this.requireShift = requireShift;
    this.requireCtrl = requireCtrl;
    this.requireAlt = requireAlt;
    this.listenForKeys = new HashSet<>(Arrays.asList(listenForKeys));
  }
}
