package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimRect;

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

    @Override
    public String toString() {
      return String.format("l=%s, r=%s, t=%s, b=%s", this.left.toString(), this.right.toString(), this.top.toString(), this.bottom.toString());
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
}
