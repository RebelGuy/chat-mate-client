package dev.rebel.chatmate.gui.StateManagement;


import java.lang.reflect.Field;
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
}
