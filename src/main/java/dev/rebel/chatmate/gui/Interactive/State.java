package dev.rebel.chatmate.gui.Interactive;


import java.lang.reflect.Field;
import java.util.Date;
import java.util.function.Consumer;

public class State<TState> {
  /** When setting the state, `property = NULL` is interpreted as the property being set to null.
   * In contrast, `property = null` is interpreted as the property *not being set*. */
  public final static Object NULL = new Object();

  protected TState state;

  public State(TState initialState) {
    this.state = initialState;
  }

  public void setState(TState state) {
    for (Field field : state.getClass().getFields()) {
      try {
        Object value = field.get(state);
        if (value == null) {
          // this implies the property was not set, so ignore it
          continue;
        }
        field.set(this.state, value == NULL ? null : value);
      } catch (IllegalAccessException ignored) { }
    }
  }

  public void setState(Consumer<TState> stateModifier) {
    stateModifier.accept(this.state);
  }

  public TState getState() {
    return this.state;
  }

  public static class Animated<T> {
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

  public static class AnimatedBool extends Animated<Boolean> {
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
}
