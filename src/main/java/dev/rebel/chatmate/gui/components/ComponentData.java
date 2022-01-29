package dev.rebel.chatmate.gui.components;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class ComponentData<T> {
  public abstract T copy();

  public abstract boolean compareTo(T other);

  public static abstract class ControllerProps<T> extends ComponentData<T> {

  }

  public static abstract class ControllerPropsWithChildren<T> extends ControllerProps<T> {
    public Component.Children children = new Component.Children();

    public ControllerPropsWithChildren<T> withChildren(Component.IChild... children) {
      // cannot already have children - once a component is defined in another component's children,
      // it is never directly worked with again until it's actually rendered down the line.
      this.children = new Component.Children(children);
      return this;
    }
  }

  public static abstract class ViewProps<T> extends ComponentData<T> {

  }

  public static abstract class ViewPropsWithChildren<T> extends ViewProps<T> {
    public Component.Children children = new Component.Children();
  }

  public static abstract class ViewState<T> extends ComponentData<T> {

  }
}
