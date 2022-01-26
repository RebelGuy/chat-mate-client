package dev.rebel.chatmate.gui.components;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class ComponentData<T> {
  public abstract T copy();

  public abstract boolean compareTo(T other);

  public static abstract class ControllerProps<T> extends ComponentData<T> {

  }

  public static abstract class ViewProps<T> extends ComponentData<T> {

  }

  public static abstract class ViewState<T> extends ComponentData<T> {

  }
}
