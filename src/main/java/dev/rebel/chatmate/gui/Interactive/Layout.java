package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimRect;

import java.util.Objects;

public class Layout {
  /** fullBox - margin - padding */
  public static DimRect getContentRect(DimRect fullBox, RectExtension margin, RectExtension padding) {
    return margin.plus(padding).applySubtractive(fullBox);
  }

  /** fullBox - margin */
  public static DimRect getCollidableRect(DimRect fullBox, RectExtension margin, RectExtension padding) {
    return margin.applySubtractive(fullBox);
  }

  public static class RectExtension {
    public final Dim left;
    public final Dim right;
    public final Dim top;
    public final Dim bottom;

    public RectExtension(Dim amount) {
      this(amount, amount, amount, amount);
    }

    public RectExtension(Dim leftRight, Dim topBottom) {
      this(leftRight, leftRight, topBottom, topBottom);
    }

    public RectExtension (Dim left, Dim right, Dim top, Dim bottom) {
      this.left = left;
      this.right = right;
      this.top = top;
      this.bottom = bottom;
    }

    public RectExtension plus(RectExtension other) {
      return new RectExtension(this.left.plus(other.left), this.right.plus(other.right), this.top.plus(other.top), this.bottom.plus(other.bottom));
    }

    public DimRect applyAdditive(DimRect rect) {
      Dim newX = rect.getX().minus(this.left);
      Dim newWidth = rect.getWidth().plus(this.left).plus(this.right);
      Dim newY = rect.getY().minus(this.top);
      Dim newHeight = rect.getHeight().plus(this.top).plus(this.bottom);
      return new DimRect(newX, newY, newWidth, newHeight);
    }

    public DimRect applySubtractive(DimRect rect) {
      Dim newX = rect.getX().plus(this.left);
      Dim newWidth = rect.getWidth().minus(this.left).minus(this.right);
      Dim newY = rect.getY().plus(this.top);
      Dim newHeight = rect.getHeight().minus(this.top).minus(this.bottom);
      return new DimRect(newX, newY, newWidth, newHeight);
    }

    public Dim getExtendedWidth() { return this.left.plus(this.right); }

    public Dim getExtendedHeight() { return this.top.plus(this.bottom); }

    /** Creates an extension that, when applied additively to the first rect, will result in the second rect. */
    public static RectExtension extendTo(DimRect from, DimRect to) {
      return new RectExtension(
          from.getX().minus(to.getX()),
          to.getRight().minus(from.getRight()),
          from.getY().minus(to.getY()),
          to.getBottom().minus(from.getBottom())
      );
    }

    public RectExtension left(Dim left) {
      return new RectExtension(left, this.right, this.top, this.bottom);
    }

    public RectExtension right(Dim right) {
      return new RectExtension(this.left, right, this.top, this.bottom);
    }

    public RectExtension top(Dim top) {
      return new RectExtension(this.left, this.right, top, this.bottom);
    }

    public RectExtension bottom(Dim bottom) {
      return new RectExtension(this.left, this.right, this.top, bottom);
    }

    // the static method names cannot be the same as the instance method names :/

    public static RectExtension fromLeft(Dim left) {
      return new RectExtension(left, left.setGui(0), left.setGui(0), left.setGui(0));
    }

    public static RectExtension fromRight(Dim right) {
      return new RectExtension(right.setGui(0), right, right.setGui(0), right.setGui(0));
    }

    public static RectExtension fromTop(Dim top) {
      return new RectExtension(top.setGui(0), top.setGui(0), top, top.setGui(0));
    }

    public static RectExtension fromBottom(Dim bottom) {
      return new RectExtension(bottom.setGui(0), bottom.setGui(0), bottom.setGui(0), bottom);
    }

    @Override
    public String toString() {
      return String.format("l=%s, r=%s, t=%s, b=%s", this.left.toString(), this.right.toString(), this.top.toString(), this.bottom.toString());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RectExtension other = (RectExtension)o;
      return this.left.equals(other.left) && this.right.equals(other.right) && this.top.equals(other.top) && this.bottom.equals(other.bottom);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.left.hashCode(), this.right.hashCode(), this.top.hashCode(), this.bottom.hashCode());
    }
  }

  public enum HorizontalAlignment {
    LEFT, CENTRE, RIGHT
  }

  public enum VerticalAlignment {
    TOP, MIDDLE, BOTTOM
  }

  /** Desired behaviour for how an element should use the available space when calculating its size. */
  public enum SizingMode {
    FILL, MINIMISE, ANY
  }

  // often times we need to set an elements true size without it affecting the layout of its parents so that it can receive interaction events.
  public enum LayoutGroup {
    /** The element participates fully in the layout calculations. */
    ALL,
    /** The element is placed statically as part of its ancestors/siblings, but its size has no effect on the ancestors/siblings. */
    CHILDREN
  }
}
