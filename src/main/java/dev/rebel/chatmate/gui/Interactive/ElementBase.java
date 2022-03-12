package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;

// a note about box size terminology:
// the full box includes contents, surrounded by padding, surrounded by margin. it is the box used when calculating any sort of layout.
// the collision box includes contents, surrounded by padding. it is the box used when checking for mouse pointer collisions with this element.
// the content box includes contents only. this is where the element's contents are rendered into.

public abstract class ElementBase implements IElement {
  protected final InteractiveContext context;
  protected final IElement parent;
  protected final Dim ZERO;

  private DimRect box;
  private RectExtension padding;
  private RectExtension margin;

  public ElementBase(InteractiveContext context, IElement parent) {
    this.context = context;
    this.parent = parent;
    this.ZERO = context.dimFactory.zeroGui();

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
  public IElement setPadding(RectExtension padding) {
    this.padding = padding;
    return this;
  }

  @Override
  public RectExtension getPadding() {
    return this.padding == null ? new RectExtension(this.context.dimFactory.zeroGui()) : this.padding;
  }

  @Override
  public IElement setMargin(RectExtension margin) {
    this.margin = margin;
    return this;
  }

  @Override
  public RectExtension getMargin() {
    return this.margin == null ? new RectExtension(this.context.dimFactory.zeroGui()) : this.margin;
  }

  protected boolean checkCollision(DimPoint point) {
    DimRect box = this.getBox();
    if (box == null) {
      return false;
    }

    return this.getMargin().applySubtractive(box).checkCollision(point);
  }

  protected Dim getContentBoxX(Dim fullBoxX) {
    return fullBoxX.plus(this.margin.left).plus(this.padding.left);
  }

  protected Dim getContentBoxY(Dim fullBoxY) {
    return fullBoxY.plus(this.margin.top).plus(this.padding.top);
  }

  protected Dim getContentBoxWidth(Dim fullBoxWidth) {
    return fullBoxWidth.minus(this.padding.left).minus(this.padding.right).minus(this.margin.left).minus(this.margin.right);
  }

  protected DimPoint getFullBoxSize(DimPoint contentBoxSize) {
    return new DimPoint(
      contentBoxSize.getX().plus(this.padding.left).plus(this.padding.right).plus(this.margin.left).plus(this.margin.right),
      contentBoxSize.getY().plus(this.padding.top).plus(this.padding.bottom).plus(this.margin.top).plus(this.margin.bottom)
    );
  }

  protected DimRect getContentBox() {
    return getContentBox(this);
  }

  protected DimRect getCollisionBox() { return getCollisionBox(this); }

  protected static DimRect getContentBox(IElement element) {
    return element.getPadding().plus(element.getMargin()).applySubtractive(element.getBox());
  }

  protected static DimRect getCollisionBox(IElement element) {
    return element.getMargin().applySubtractive(element.getBox());
  }

  protected static DimRect getFullBox(IElement element) {
    return element.getBox();
  }
}
