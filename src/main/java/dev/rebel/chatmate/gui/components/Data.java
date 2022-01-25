package dev.rebel.chatmate.gui.components;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class Data<T> {
  public abstract T copy();
  public abstract boolean compareTo(T other);
}

public abstract class PropsData<T> extends Data<T> {
  public final PropsData<T> withChildren(ComponentManager.StaticComponent nextConnected) {
    // for chaining children - only if the component supports children.
    // perhaps modify the static creator function directly so it accepts a variable amount of children...
    // then tack it onto the props?

    return this;
  }
}
