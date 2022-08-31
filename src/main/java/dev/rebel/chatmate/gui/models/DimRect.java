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

  public DimPoint getTopLeft() { return new DimPoint(this.getX(), this.getY()); }

  public DimPoint getTopCentre() { return new DimPoint(this.getX().plus(this.getWidth().over(2)), this.getY()); }

  public DimPoint getTopRight() { return new DimPoint(this.getRight(), this.getY()); }

  public DimPoint getBottomLeft() { return new DimPoint(this.getX(), this.getBottom()); }

  public DimPoint getBottomCentre() { return new DimPoint(this.getX().plus(this.getWidth().over(2)), this.getBottom()); }

  public DimPoint getBottomRight() { return new DimPoint(this.getRight(), this.getBottom()); }

  public DimPoint getLeftCentre() { return new DimPoint(this.getX(), this.getY().plus(this.getHeight().over(2))); }

  public DimPoint getCentre() { return new DimPoint(this.getX().plus(this.getWidth().over(2)), this.getY().plus(this.getHeight().over(2))); }

  public DimPoint getRightCentre() { return new DimPoint(this.getRight(), this.getY().plus(this.getHeight().over(2))); }

  /** Returns true if the point is contained within the rect, or touches its boundary. */
  public boolean checkCollision(DimPoint point) {
    return this.x.lte(point.getX())
        && this.x.plus(this.width).gte(point.getX())
        && this.y.lte(point.getY())
        && this.y.plus(this.height).gte(point.getY());
  }

  public boolean checkCollisionX(Dim x) {
    return this.x.lte(x) && this.getRight().gte(x);
  }

  public boolean checkCollisionY(Dim y) {
    return this.y.lte(y) && this.getBottom().gte(y);
  }

  public DimRect withTranslation(DimPoint translation) {
    return new DimRect(this.getPosition().plus(translation), this.getSize());
  }

  public DimRect withSize(DimPoint size) {
    return new DimRect(this.getPosition(), size);
  }

  public DimRect withPosition(DimPoint position) {
    return new DimRect(position, this.getSize());
  }

  public DimRect withLeft(Dim left) { return new DimRect(left, this.y, this.width, this.height); }

  public DimRect withRight(Dim right) { return new DimRect(this.x, this.y, right.minus(this.x), this.height); }

  public DimRect withTop(Dim top) { return new DimRect(this.x, top, this.width, this.height); }

  public DimRect withBottom(Dim bottom) { return new DimRect(this.x, this.y, this.width, bottom.minus(this.y)); }

  public DimRect withWidth(Dim width) { return new DimRect(this.x, this.y, width, this.height); }

  public DimRect withHeight(Dim height) { return new DimRect(this.x, this.y, this.width, height); }

  /** Truncates the sides of this rect such that it fits into the given rect (loose fit). */
  public DimRect clamp(DimRect other) {
    Dim x = Dim.max(this.getX(), other.getX());
    Dim y = Dim.max(this.getY(), other.getY());
    Dim right = Dim.min(this.getRight(), other.getRight());
    Dim bottom = Dim.min(this.getBottom(), other.getBottom());

    return new DimRect(x, y, right.minus(x), bottom.minus(y));
  }

  public DimRect partial(float fromX, float toX, float fromY, float toY) {
    return new DimRect(
        this.x.plus(this.width.times(fromX)),
        this.y.plus(this.height.times(fromY)),
        this.width.times(toX - fromX),
        this.height.times(toY - fromY)
    );
  }

  @Override
  public String toString() {
    return String.format("{%s, %s}", this.getPosition().toString(), this.getSize().toString());
  }
}
