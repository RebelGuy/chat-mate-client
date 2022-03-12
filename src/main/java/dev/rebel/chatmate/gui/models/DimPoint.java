package dev.rebel.chatmate.gui.models;

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
}
