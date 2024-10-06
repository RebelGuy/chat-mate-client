package dev.rebel.chatmate.gui.StateManagement;


import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Function;

public class State<TState> {
  /** When setting the state, `property = NULL` is interpreted as the property being set to null.
   * In contrast, `property = null` is interpreted as the property *not being set*. */
  public final static Object NULL = new Object();

  protected TState state;

  public State(TState initialState) {
    this.state = initialState;
  }

  public void setState(TState state) {
    if (ClassUtils.isPrimitiveOrWrapper(state.getClass())) {
      this.state = state;
      return;
    } else if (!OuterState.class.isAssignableFrom(state.getClass())) {
      this.state = state;
      return;
    }

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

  /** For overwriting this state. */
  public void setState(Function<TState, TState> stateModifier) {
    this.setState(stateModifier.apply(this.state));
  }

  /** For modifying this state. */
  public void accessState(Consumer<TState> stateAccessor) {
    stateAccessor.accept(this.state);
  }

  public TState getState() {
    return this.state;
  }

  /** If your state type extends this class, you can set the state of individual fields (equivalent to having multiple State<> objects). */
  public static class OuterState { }
}
