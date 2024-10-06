package dev.rebel.chatmate.gui.StateManagement;

import java.util.function.Consumer;

public class AnimatedBool extends Animated<Boolean> {
  public AnimatedBool(Long duration, boolean initialValue, Consumer<Boolean> onChange) {
    super(duration, initialValue, onChange);
  }

  public AnimatedBool(Long duration, boolean initialValue) {
    super(duration, initialValue);
  }

  /** Can be interpreted as a continuous boolean, where 0 corresponds to `false` and 1 corresponds to `true`. */
  @Override
  public float getFrac() {
    float frac = super.getFrac();
    if (super.value) {
      return frac;
    } else {
      return 1 - frac;
    }
  }

  /** Returns the new value. */
  public boolean flip() {
    super.set(!super.value);
    return super.value;
  }
}
