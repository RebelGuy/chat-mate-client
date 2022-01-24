package dev.rebel.chatmate.gui.components;

import javax.annotation.concurrent.Immutable;

@Immutable
public abstract class Data<T> {
  public abstract T copy();
  public abstract boolean compareTo(T other);
}
