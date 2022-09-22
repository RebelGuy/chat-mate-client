package dev.rebel.chatmate.gui.StateManagement;

import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Timer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Animated<T> {
  /** Number of milliseconds over which the animation occurs. */
  public final Long duration;
  private final @Nullable Consumer<T> onChange;
  private @Nullable Timer timer;
  private @Nullable Function<Float, Float> easingFunction;

  protected Long prevTime;
  protected T prevValue;
  protected T value;

  public Animated(Long duration, T initialValue) {
    this(duration, initialValue, null);
  }

  /** `onChange` is called when the animation is complete, and will provide the new value. */
  public Animated(Long duration, T initialValue, @Nullable Consumer<T> onChange) {
    this.duration = duration;
    this.onChange = onChange;
    this.timer = onChange == null ? null : new Timer();

    this.prevValue = initialValue;
    this.value = initialValue;
    this.prevTime = 0L;
  }

  /** Sets a function to use for transforming the frac. The input will always be 0 <= x <= 1. */
  public void setEasing(Function<Float, Float> easingFunction) {
    this.easingFunction = easingFunction;
  }

  public void set(T newValue) {
    // by checking if the value has changed, we ensure that setting the same value twice in quick succession
    // doesn't reset the animation
    if (this.value == newValue) {
      return;
    }

    this.prevValue = this.value;
    this.value = newValue;
    this.prevTime = timestamp();

    if (this.onChange != null) {
      this.timer.cancel();
      this.timer = new Timer();
      this.timer.schedule(new TaskWrapper(() -> this.onChange.accept(this.get())), this.duration);
    }
  }

  /** Does not call the `onChange` callback, if it was set. */
  public void setImmediate(T newValue) {
    if (this.value == newValue) {
      return;
    }

    this.prevValue = this.value;
    this.value = newValue;
    this.prevTime = 0L;

    if (this.onChange != null) {
      this.timer.cancel();
    }
  }

  public T get() {
    return this.value;
  }

  public T getTarget() {
    return this.value;
  }

  /** Returns the current animation fraction, 0 <= frac <= 1. If set, the frac will first pass through the easing function before being returned. */
  public float getFrac() {
    Long now = timestamp();
    float frac = (float)(now - prevTime) / this.duration;
    frac = frac < 0 ? 0 : frac > 1 ? 1 : frac;
    return this.easingFunction == null ? frac : this.easingFunction.apply(frac);
  }

  private static Long timestamp() {
    return new Date().getTime();
  }
}
