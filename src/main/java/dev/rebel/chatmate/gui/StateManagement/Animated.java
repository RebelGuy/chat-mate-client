package dev.rebel.chatmate.gui.StateManagement;

import java.util.Date;

public class Animated<T> {
  /** Number of milliseconds over which the animation occurs. */
  public final Long duration;

  protected Long prevTime;
  protected T prevValue;
  protected T value;

  public Animated(Long duration, T initialValue) {
    this.duration = duration;

    this.prevValue = initialValue;
    this.value = initialValue;
    this.prevTime = 0L;
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
  }

  public void setImmediate(T newValue) {
    if (this.value == newValue) {
      return;
    }

    this.prevValue = this.value;
    this.value = newValue;
    this.prevTime = 0L;
  }

  public T get() {
    return this.value;
  }

  public float getFrac() {
    Long now = timestamp();
    float frac = (float)(now - prevTime) / this.duration;
    return frac < 0 ? 0 : frac > 1 ? 1 : frac;
  }

  private static Long timestamp() {
    return new Date().getTime();
  }
}
