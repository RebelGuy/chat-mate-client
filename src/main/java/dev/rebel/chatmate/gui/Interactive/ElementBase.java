package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;

public abstract class ElementBase implements IElement {
  protected final InteractiveContext context;
  protected final IElement parent;

  protected DimRect box;
  protected RectExtension padding;
  protected RectExtension margin;

  public ElementBase(InteractiveContext context, IElement parent) {
    this.context = context;
    this.parent = parent;

    this.box = null;
    this.padding = new RectExtension(context.dimFactory.zeroGui());
    this.margin = new RectExtension(context.dimFactory.zeroGui());
  }

  @Override
  public void onCreate() { }

  @Override
  public void onDispose() { }

  @Override
  public boolean onMouseDown(In in) {
    return false;
  }

  @Override
  public boolean onMouseMove(In in) {
    return false;
  }

  @Override
  public boolean onMouseUp(In in) {
    return false;
  }

  @Override
  public boolean onMouseScroll(In in) {
    return false;
  }

  @Override
  public boolean onKeyDown(KeyboardEventData.In in) {
    return false;
  }

  @Override
  public void onInvalidateSize() {
    this.parent.onInvalidateSize();
  }

  @Override
  public void setBox(DimRect box) {
    this.box = box;
  }

  @Override
  public DimRect getBox() {
    return this.box;
  }

  @Override
  public void setPadding(RectExtension padding) {
    this.padding = padding;
  }

  @Override
  public RectExtension getPadding() {
    return this.padding;
  }

  @Override
  public void setMargin(RectExtension margin) {
    this.margin = margin;
  }

  @Override
  public RectExtension getMargin() {
    return this.margin;
  }

  protected boolean checkCollision(DimPoint point) {
    DimRect box = this.getBox();
    if (box == null) {
      return false;
    }

    return this.getMargin().applySubtractive(box).checkCollision(point);
  }
}
