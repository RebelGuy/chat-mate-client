package dev.rebel.chatmate.gui.models;

import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.TextHelpers;

import java.util.List;
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
    return new Dim(this.scaleFactor, this.anchor).withValue(screenValue * this.getConversionFactor(DimAnchor.SCREEN, this.anchor));
  }

  public Dim setGui(float guiValue) {
    return new Dim(this.scaleFactor, this.anchor).withValue(guiValue * this.getConversionFactor(DimAnchor.GUI, this.anchor));
  }

  public Dim setAnchor(DimAnchor anchor) {
    return new Dim(this.scaleFactor, anchor).withValue(this.value * this.getConversionFactor(this.anchor, anchor));
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

  /** Dim units cancel out - this is a ratio. */
  public float over(Dim other) {
    return this.value / this.getOtherValue(other);
  }

  public Dim copy() {
    return new Dim(this.scaleFactor, this.anchor).withValue(this.value);
  }

  public boolean lte(Dim other) { return this.value <= this.getOtherValue(other); }

  public boolean gte(Dim other) { return this.value >= this.getOtherValue(other); }

  public boolean lt(Dim other) { return this.value < this.getOtherValue(other); }

  public boolean gt(Dim other) { return this.value > this.getOtherValue(other); }

  public Dim ceil() { return new Dim(this.scaleFactor, this.anchor).withValue((float)Math.ceil(this.value)); }

  /** Rounds to two decimal places. */
  public Dim round() { return new Dim(this.scaleFactor, this.anchor).withValue((float)Math.round(this.value * 100) / 100.0f); }

  public static Dim max(Dim a, Dim b) { return a.gte(b) ? a : b; }

  public static Dim max(List<Dim> items) {
    return Collections.eliminate(items, Dim::max);
  }

  public static Dim max(Dim... items) {
    return Dim.max(Collections.list(items));
  }

  public static Dim min(Dim a, Dim b) { return a.lte(b) ? a : b; }

  public static Dim clamp(Dim x, Dim min, Dim max) {
    return Dim.min(Dim.max(x, min), max);
  }

  public static Dim sum(List<Dim> items) {
    return Collections.eliminate(items, Dim::plus);
  }

  public static boolean positive(Dim x) { return x != null && x.value > 0; }

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

  /** Mutates the value. For internal use only - it is the caller's responsibility that the units of `value` are correct. */
  private Dim withValue(float value) {
    this.value = value;
    return this;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", TextHelpers.float2Str(this.value), this.anchor);
  }

  public enum DimAnchor { SCREEN, GUI }
}
