package dev.rebel.chatmate.gui.StateManagement;

import java.util.Objects;
import java.util.function.Consumer;

public class AnimatedFloat extends Animated<Float> {
  public AnimatedFloat(Long duration, float initialValue, Consumer<Float> onChange) {
    super(duration, initialValue, onChange);
  }

  public AnimatedFloat(Long duration, float initialValue) {
    super(duration, initialValue);
  }

  /** Gets the current value. */
  @Override
  public Float get() {
    float frac = super.getFrac();
    float value = super.value;
    float prevValue = super.prevValue;
    return frac * (value - prevValue) + prevValue;
  }

  @Override
  public void set(Float newValue) {
    float frac = super.getFrac();
    float prevValue = this.get();

    super.set(newValue);

    // if we updated the value before completing the animation, set the old prevValue.
    // this will continue the animation spatially to avoid jumps (but may be a bit janky temporally, depending on the easing function)
    if (frac < 1) {
      super.prevValue = prevValue;
    }
  }

  /** Translates the animated value immediately. */
  public void translate(float delta) {
    super.prevValue += delta;
    super.value += delta;
  }
}
