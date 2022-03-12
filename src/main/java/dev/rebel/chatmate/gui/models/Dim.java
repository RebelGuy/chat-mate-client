package dev.rebel.chatmate.gui.models;

import java.util.Objects;
import java.util.function.Supplier;

/** A `Dim` has its underlying value immutably anchored to the screen or GUI. Note: GlStateManager uses Gui values. */
public class Dim {
  private final Supplier<Integer> scaleFactor;
  private float value;

  public final DimAnchor anchor;

  public Dim(Supplier<Integer> scaleFactor, DimAnchor anchor) {
    this.scaleFactor = scaleFactor;
    this.anchor = anchor;
  }

  public float getScreen() {
    return this.value * this.getConversionFactor(this.anchor, DimAnchor.SCREEN);
  }

  public float getGui() {
    return this.value * this.getConversionFactor(this.anchor, DimAnchor.GUI);
  }

  public Dim setScreen(float screenValue) {
    this.value = screenValue * this.getConversionFactor(DimAnchor.SCREEN, this.anchor);
    return this;
  }

  public Dim setGui(float guiValue) {
    this.value = guiValue * this.getConversionFactor(DimAnchor.GUI, this.anchor);
    return this;
  }

  public Dim set(Dim other) {
    this.value = this.getOtherValue(other);
    return this;
  }

  public Dim plus(Dim other) {
    return this.copy().withValue(this.value + this.getOtherValue(other));
  }

  public Dim minus(Dim other) {
    return this.copy().withValue(this.value - this.getOtherValue(other));
  }

  public Dim times(float other) {
    return this.copy().withValue(this.value * other);
  }

  public Dim over(float other) {
    return this.copy().withValue(this.value / other);
  }

  public Dim copy() {
    return new Dim(this.scaleFactor, this.anchor).set(this);
  }

  public boolean lte(Dim other) { return this.value <= this.getOtherValue(other); }

  public boolean gte(Dim other) { return this.value >= this.getOtherValue(other); }

  public boolean lt(Dim other) { return this.value < this.getOtherValue(other); }

  public boolean gt(Dim other) { return this.value > this.getOtherValue(other); }

  public static Dim max(Dim a, Dim b) { return a.gte(b) ? a : b; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dim dim = (Dim)o;
    return dim.anchor == this.anchor && Float.compare(dim.value, this.value) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaleFactor, value);
  }

  private float getOtherValue(Dim other) {
    if (this.anchor == DimAnchor.SCREEN) {
      return other.getScreen();
    } else if (this.anchor == DimAnchor.GUI) {
      return other.getGui();
    } else {
      throw new RuntimeException("Did not expect to get here");
    }
  }

  private float getConversionFactor(DimAnchor from, DimAnchor to) {
    if (from == to) {
      return 1;
    } else if (from == DimAnchor.SCREEN) {
      return 1.0f / this.scaleFactor.get();
    } else if (from == DimAnchor.GUI) {
      return this.scaleFactor.get();
    } else {
      throw new RuntimeException("Did not expect to get here");
    }
  }

  /** For internal use only - it is the caller's responsibility that the units of `value` are correct. */
  private Dim withValue(float value) {
    this.value = value;
    return this;
  }

  public enum DimAnchor { SCREEN, GUI }
}
