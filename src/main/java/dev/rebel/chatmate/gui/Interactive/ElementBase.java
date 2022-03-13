package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
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

  protected DimPoint lastCalculatedSize;
  private DimRect box;
  private RectExtension padding;
  private RectExtension margin;
  private HorizontalAlignment horizontalAlignment;
  private VerticalAlignment verticalAlignment;

  public ElementBase(InteractiveContext context, IElement parent) {
    this.context = context;
    this.parent = parent;
    this.ZERO = context.dimFactory.zeroGui();

    this.box = null;
    this.padding = new RectExtension(context.dimFactory.zeroGui());
    this.margin = new RectExtension(context.dimFactory.zeroGui());
    this.horizontalAlignment = HorizontalAlignment.LEFT;
    this.verticalAlignment = VerticalAlignment.TOP;
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

  /** Should be called after the onCalculateSize() method is complete.
   * The reason this is abstract is to remind the element to update this property. */
  // todo: this is actually kinda yucky - maybe the correct solution is to create some kind of wrapper class around each element,
  // which can be used as an additional layer between parents calling methods on the child, and the child calling methods on (or passing data to) the parent.
  protected abstract DimPoint setLastCalculatedSize(DimPoint size);

  @Override
  public DimPoint getLastCalculatedSize() {
    return this.lastCalculatedSize;
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

  @Override
  public IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
    return this;
  }

  @Override
  public HorizontalAlignment getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public IElement setVerticalAlignment(VerticalAlignment verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
    return this;
  }

  @Override
  public VerticalAlignment getVerticalAlignment() {
    return this.verticalAlignment;
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

  protected DimRect alignChild(IElement child) {
    return alignElementInBox(child.getLastCalculatedSize(), this.getContentBox(), child.getHorizontalAlignment(), child.getVerticalAlignment());
  }

  /** Lays out the size within the given box, using the provided alignment options.
   * Note: If the size is larger than the provided box, it will NOT be contained entirely. */
  protected static DimRect alignElementInBox(DimPoint size, DimRect box, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
    Dim x;
    switch (horizontalAlignment) {
      case LEFT:
        x = box.getX();
        break;
      case CENTRE:
        x = box.getX().plus(box.getWidth().minus(size.getX()).over(2));
        break;
      case RIGHT:
        x = box.getRight().minus(size.getX());
        break;
      default:
        throw new RuntimeException("Invalid HorizontalAlignment " + horizontalAlignment);
    }

    Dim y;
    switch (verticalAlignment) {
      case TOP:
        y = box.getY();
        break;
      case MIDDLE:
        y = box.getY().plus(box.getHeight().minus(size.getY()).over(2));
        break;
      case BOTTOM:
        y = box.getBottom().minus(size.getY());
        break;
      default:
        throw new RuntimeException("Invalid VerticalAlignment " + verticalAlignment);
    }

    return new DimRect(new DimPoint(x, y), size);
  }
}
