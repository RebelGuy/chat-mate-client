package dev.rebel.chatmate.gui.models;

public class DimRect {
  private final Dim x;
  private final Dim y;
  private final Dim width;
  private final Dim height;

  public DimRect(Dim x, Dim y, Dim width, Dim height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public DimRect(DimPoint position, DimPoint size) {
    this(position.getX(), position.getY(), size.getX(), size.getY());
  }

  public Dim getX() {
    return this.x;
  }

  public Dim getY() {
    return this.y;
  }

  public Dim getWidth() {
    return this.width;
  }

  public Dim getHeight() {
    return this.height;
  }

  public Dim getRight() { return this.x.plus(this.width); }

  public Dim getBottom() { return this.y.plus(this.height); }

  public DimPoint getPosition() {
    return new DimPoint(this.x, this.y);
  }

  public DimPoint getSize() {
    return new DimPoint(this.width, this.height);
  }

  /** Returns true if the point is contained within the rect, or touches its boundary. */
  public boolean checkCollision(DimPoint point) {
    return this.x.lte(point.getX())
        && this.x.plus(this.width).gte(point.getX())
        && this.y.lte(point.getY())
        && this.y.plus(this.height).gte(point.getY());
  }

  public DimRect withTranslation(DimPoint translation) {
    return new DimRect(this.getPosition().plus(translation), this.getSize());
  }

  public DimRect withSize(DimPoint size) {
    return new DimRect(this.getPosition(), size);
  }

  /** Truncates the sides of this rect such that it fits into the given rect (loose fit). */
  public DimRect clamp(DimRect other) {
    Dim x = Dim.max(this.getX(), other.getX());
    Dim y = Dim.max(this.getY(), other.getY());
    Dim right = Dim.min(this.getRight(), other.getRight());
    Dim bottom = Dim.min(this.getBottom(), other.getBottom());

    return new DimRect(x, y, right.minus(x), bottom.minus(y));
  }
}
