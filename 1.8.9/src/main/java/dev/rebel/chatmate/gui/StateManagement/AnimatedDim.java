package dev.rebel.chatmate.gui.StateManagement;

import dev.rebel.chatmate.gui.models.Dim;

import java.util.function.Consumer;

public class AnimatedDim extends Animated<Dim> {
  public AnimatedDim(Long duration, Dim initialValue, Consumer<Dim> onChange) {
    super(duration, initialValue, onChange);
  }

  public AnimatedDim(Long duration, Dim initialValue) {
    super(duration, initialValue);
  }

  /** Gets the current value. */
  @Override
  public Dim get() {
    float frac = super.getFrac();
    Dim value = super.value;
    Dim prevValue = super.prevValue;
    return value.minus(prevValue).times(frac).plus(prevValue); // interpolate
  }

  @Override
  public void set(Dim newValue) {
    float frac = super.getFrac();
    Dim prevValue = this.get();

    super.set(newValue);

    // if we updated the value before completing the animation, set the old prevValue.
    // this will continue the animation spatially to avoid jumps (but may be a bit janky temporally, depending on the easing function)
    if (frac < 1) {
      super.prevValue = prevValue;
    }
  }

  /** Translates the animated value immediately. */
  public void translate(Dim delta) {
    super.prevValue = super.prevValue.plus(delta);;
    super.value = super.value.plus(delta);
  }
}
