package dev.rebel.chatmate.gui.models;

import java.util.Objects;
import java.util.function.Supplier;

public class Dim {
  private final Supplier<Integer> scaleFactor;
  private float value;

  public Dim(Supplier<Integer> scaleFactor) {
    this.scaleFactor = scaleFactor;
  }

  public float getScreen() {
    return this.value;
  }

  public float getGui() {
    return this.value / this.scaleFactor.get();
  }

  public Dim setScreen(float screenValue) {
    this.value = screenValue;
    return this;
  }

  public Dim setGui(float guiValue) {
    this.value = guiValue * this.scaleFactor.get();
    return this;
  }

  public Dim set(Dim dim) {
    this.value = dim.value;
    return this;
  }

  public Dim plus(Dim other) {
    return this.copy().setScreen(this.value + other.getScreen());
  }

  public Dim minus(Dim other) {
    return this.copy().setScreen(this.value - other.getScreen());
  }

  public Dim times(Dim other) {
    return this.copy().setScreen(this.value * other.getScreen());
  }

  public Dim times(float other) {
    return this.copy().setScreen(this.value * other);
  }

  public Dim over(Dim other) {
    return this.copy().setScreen(this.value / other.getScreen());
  }

  public Dim over(float other) {
    return this.copy().setScreen(this.value / other);
  }

  public Dim copy() {
    return new Dim(this.scaleFactor).set(this);
  }

  public boolean lte(Dim other) { return this.getScreen() <= other.getScreen(); }

  public boolean gte(Dim other) { return this.getScreen() >= other.getScreen(); }

  public boolean lt(Dim other) { return this.getScreen() < other.getScreen(); }

  public boolean gt(Dim other) { return this.getScreen() > other.getScreen(); }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dim dim = (Dim)o;
    return Float.compare(dim.value, this.value) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaleFactor, value);
  }
}
