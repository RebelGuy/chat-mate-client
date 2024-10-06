package dev.rebel.chatmate.gui.models;

import dev.rebel.chatmate.gui.models.Dim.DimAnchor;

public class DimPoint {
  private final Dim x;
  private final Dim y;

  public DimPoint(Dim x, Dim y) {
    this.x = x;
    this.y = y;
  }

  public Dim getX() {
    return this.x;
  }

  public Dim getY() {
    return this.y;
  }

  public DimPoint plus(DimPoint other) {
    return new DimPoint(this.x.plus(other.getX()), this.y.plus(other.getY()));
  }

  public DimPoint minus(DimPoint other) {
    return new DimPoint(this.x.minus(other.getX()), this.y.minus(other.getY()));
  }

  public DimPoint setAnchor(DimAnchor anchor) {
    return new DimPoint(this.getX().setAnchor(anchor), this.getY().setAnchor(anchor));
  }

  /** Scales the values by the given number. */
  public DimPoint scale(float scale) {
    return new DimPoint(this.x.times(scale), this.y.times(scale));
  }

  /** Scales the values independently by the given numbers. */
  public DimPoint scale(float scaleX, float scaleY) {
    return new DimPoint(this.x.times(scaleX), this.y.times(scaleY));
  }

  public Dim distanceTo(DimPoint other) {
    float deltaX = this.getX().minus(other.getX()).getGui();
    float deltaY = this.getY().minus(other.getY()).getGui();

    return this.x.setGui((float)Math.sqrt(deltaX * deltaX + deltaY * deltaY));
  }

  @Override
  public String toString() {
    return String.format("(%s, %s)", this.x.toString(), this.y.toString());
  }
}
