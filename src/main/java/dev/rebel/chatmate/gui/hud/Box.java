package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;

public class Box {
  private final DimFactory dimFactory;
  protected Dim x;
  protected Dim y;
  protected Dim w;
  protected Dim h;
  private boolean canTranslate;
  private boolean canResize;

  public Box(DimFactory dimFactory, Dim x, Dim y, Dim w, Dim h, boolean canTranslate, boolean canResize) {
    this.dimFactory = dimFactory;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.canResize = canResize;
    this.canTranslate = canTranslate;
  }

  public Dim getX() { return this.x.copy(); }

  public Dim getY() { return this.y.copy(); }

  public Dim getWidth() { return this.w.copy(); }

  public Dim getHeight() { return this.h.copy(); }

  public boolean canTranslate() { return this.canTranslate; }

  public void onTranslate(Dim newX, Dim newY) {
    if (this.canTranslate) {
      this.x.set(newX);
      this.y.set(newY);
    }
  }

  public boolean canResizeBox() { return this.canResize; }

  public void onResize(Dim newW, Dim newH, Anchor resizeAnchor) {
    if (!this.canResize || this.getWidth().equals(newW) && this.getHeight().equals(newH)) {
      return;
    }

    Dim dw = newW.minus(this.w);
    Dim dh = newH.minus(this.h);
    Dim zero = this.dimFactory.zeroGui();

    switch (resizeAnchor) {
      case TOP_LEFT:
        this.x = this.x.plus(zero);
        this.y = this.y.plus(zero);
        break;
      case LEFT_CENTRE:
        this.x = this.x.plus(zero);
        this.y = this.y.minus(dh.over(2));
        break;
      case BOTTOM_LEFT:
        this.x = this.x.plus(zero);
        this.y = this.y.minus(dh);
        break;

      case TOP_CENTRE:
        this.x = this.x.minus(dw.over(2));
        this.y = this.y.plus(zero);
        break;
      case MIDDLE:
        this.x = this.x.minus(dw.over(2));
        this.y = this.y.minus(dh.over(2));
        break;
      case BOTTOM_CENTRE:
        this.x = this.x.minus(dw.over(2));
        this.y = this.y.minus(dh);
        break;

      case TOP_RIGHT:
        this.x = this.x.minus(dw);
        this.y = this.y.plus(zero);
        break;
      case RIGHT_CENTRE:
        this.x = this.x.minus(dw);
        this.y = this.y.minus(dh.over(2));
        break;
      case BOTTOM_RIGHT:
        this.x = this.x.minus(dw);
        this.y = this.y.minus(dh);
        break;

      default:
        throw new RuntimeException("Invalid anchor: " + resizeAnchor);
    }

    this.w = newW;
    this.h = newH;
  }

  protected void setRect(Dim x, Dim y, Dim w, Dim h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
}
