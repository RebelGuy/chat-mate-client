package dev.rebel.chatmate.gui.Interactive.Events;

import dev.rebel.chatmate.gui.Interactive.InputElement;

import javax.annotation.Nullable;

public class FocusEventData {
  public final @Nullable
  InputElement fromFocus;
  public final @Nullable InputElement toFocus;
  public final FocusReason reason;

  public FocusEventData(@Nullable InputElement fromFocus, @Nullable InputElement toFocus, FocusReason reason) {
    this.fromFocus = fromFocus;
    this.toFocus = toFocus;
    this.reason = reason;
  }

  public enum FocusReason {
    CLICK, TAB, // the focus was triggered by the user
    CODE, // the focus was triggered programmatically
    AUTO // the focus was triggered by the InteractiveScreen
  }
}
