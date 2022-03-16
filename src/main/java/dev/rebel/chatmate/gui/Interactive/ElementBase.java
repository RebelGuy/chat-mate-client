package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.EventPhase;
import dev.rebel.chatmate.gui.Interactive.Events.EventType;
import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;

import javax.swing.*;

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
  private int zIndex;
  private boolean isFocusable;
  private HorizontalAlignment horizontalAlignment;
  private VerticalAlignment verticalAlignment;

  public ElementBase(InteractiveContext context, IElement parent) {
    this.context = context;
    this.parent = parent;
    this.ZERO = context.dimFactory.zeroGui();

    this.box = null;
    this.padding = new RectExtension(context.dimFactory.zeroGui());
    this.margin = new RectExtension(context.dimFactory.zeroGui());
    this.zIndex = 0;
    this.isFocusable = false;
    this.horizontalAlignment = HorizontalAlignment.LEFT;
    this.verticalAlignment = VerticalAlignment.TOP;
  }

  @Override
  public final IElement getParent() { return this.parent; }

  @Override
  public void onCreate() { }

  @Override
  public void onDispose() { }

  @Override
  public final void onEvent(EventType type, IEvent<?> event) {
    if (event.getPhase() == EventPhase.TARGET) {
      switch (type) {
        case FOCUS:
          break;
        case BLUR:
          break;
        default:
          throw new RuntimeException("Invalid event type at TARGET phase: " + type);
      }
    } else if (event.getPhase() == EventPhase.CAPTURE) {
      this.onEventCapture(type, event);
    } else if (event.getPhase() == EventPhase.BUBBLE) {
      this.onEventBubble(type, event);
    } else {
      throw new RuntimeException("Invalid event phase: " + event.getPhase());
    }

    // todo: look at how we could do onEnter and onExit events. definitely want the target element to use same logic as for click, and maybe also include the previous-next element in the data (ie. the before-after target elements)
  }

  private void onEventCapture(EventType type, IEvent<?> event) {
    switch (type) {
      case MOUSE_DOWN:
        this.onCaptureMouseDown((IEvent<MouseEventData.In>)event);
        break;
      case MOUSE_MOVE:
        this.onCaptureMouseMove((IEvent<MouseEventData.In>)event);
        break;
      case MOUSE_UP:
        this.onCaptureMouseUp((IEvent<MouseEventData.In>)event);
        break;
      case MOUSE_SCROLL:
        this.onCaptureMouseScroll((IEvent<MouseEventData.In>)event);
        break;
      case KEY_DOWN:
        this.onCaptureKeyDown((IEvent<KeyboardEventData.In>)event);
        break;
      default:
        throw new RuntimeException("Invalid event type at CAPTURE phase: " + type);
    }
  }

  private void onEventBubble(EventType type, IEvent<?> event) {
    switch (type) {
      case MOUSE_DOWN:
        this.onMouseDown((IEvent<MouseEventData.In>)event);
        break;
      case MOUSE_MOVE:
        this.onMouseMove((IEvent<MouseEventData.In>)event);
        break;
      case MOUSE_UP:
        this.onMouseUp((IEvent<MouseEventData.In>)event);
        break;
      case MOUSE_SCROLL:
        this.onMouseScroll((IEvent<MouseEventData.In>)event);
        break;
      case KEY_DOWN:
        this.onKeyDown((IEvent<KeyboardEventData.In>)event);
        break;
      default:
        throw new RuntimeException("Invalid event type at BUBBLE phase: " + type);
    }
  }

  public void onMouseDown(IEvent<MouseEventData.In> e) {}
  public void onCaptureMouseDown(IEvent<MouseEventData.In> e) {}
  public void onMouseMove(IEvent<MouseEventData.In> e) {}
  public void onCaptureMouseMove(IEvent<MouseEventData.In> e) {}
  public void onMouseUp(IEvent<MouseEventData.In> e) {}
  public void onCaptureMouseUp(IEvent<MouseEventData.In> e) {}
  public void onMouseScroll(IEvent<MouseEventData.In> e) {}
  public void onCaptureMouseScroll(IEvent<MouseEventData.In> e) {}
  public void onKeyDown(IEvent<KeyboardEventData.In> e) {}
  public void onCaptureKeyDown(IEvent<KeyboardEventData.In> e) {}
  public void onFocus(IEvent<FocusEventData> e) {}
  public void onBlur(IEvent<FocusEventData> e) {}

  @Override
  public final void onCloseScreen() { this.parent.onCloseScreen(); }

  @Override
  public final void onInvalidateSize() {
    this.parent.onInvalidateSize();
  }

  /** Should be called after the onCalculateSize() method is complete.
   * The reason this is abstract is to remind the element to update this property. */
  // todo: this is actually kinda yucky - maybe the correct solution is to create some kind of wrapper class around each element,
  // which can be used as an additional layer between parents calling methods on the child, and the child calling methods on (or passing data to) the parent.
  protected abstract DimPoint setLastCalculatedSize(DimPoint size);

  @Override
  public final DimPoint getLastCalculatedSize() {
    return this.lastCalculatedSize;
  }

  @Override
  public void setBox(DimRect box) {
    this.box = box;
  }

  @Override
  public final DimRect getBox() {
    return this.box;
  }

  @Override
  public final void render() {
    if (this.context.debugLayout) {
      RendererHelpers.renderRectWithCutout(1000, this.getBox(), this.getCollisionBox(), Colour.RED.withAlpha(0.1f));
      RendererHelpers.renderRectWithCutout(1000, this.getCollisionBox(), this.getContentBox(), Colour.GREEN.withAlpha(0.1f));
      RendererHelpers.renderRect(1000, this.getContentBox(), Colour.BLUE.withAlpha(0.2f));
    }

    this.renderElement();
  }

  /** You should never call super.render() from this method, as it will cause an infinite loop.
   * If you need to render a base element, use super.renderElement() instead. */
  protected abstract void renderElement();

  @Override
  public final IElement setPadding(RectExtension padding) {
    this.padding = padding;
    return this;
  }

  @Override
  public final RectExtension getPadding() {
    return this.padding == null ? new RectExtension(this.context.dimFactory.zeroGui()) : this.padding;
  }

  @Override
  public final IElement setMargin(RectExtension margin) {
    this.margin = margin;
    return this;
  }

  @Override
  public final RectExtension getMargin() {
    return this.margin == null ? new RectExtension(this.context.dimFactory.zeroGui()) : this.margin;
  }

  @Override
  public final int getZIndex() {
    return this.zIndex;
  }

  @Override
  public final IElement setZIndex(int zIndex) {
    this.zIndex = zIndex;
    return this;
  }

  @Override
  public final boolean getFocusable() {
    return this.isFocusable;
  }

  @Override
  public final IElement setFocusable(boolean focusable) {
    this.isFocusable = focusable;
    return this;
  }

  @Override
  public final IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
    return this;
  }

  @Override
  public final HorizontalAlignment getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public final IElement setVerticalAlignment(VerticalAlignment verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
    return this;
  }

  @Override
  public final VerticalAlignment getVerticalAlignment() {
    return this.verticalAlignment;
  }

  protected final boolean checkCollision(DimPoint point) {
    DimRect box = this.getBox();
    if (box == null) {
      return false;
    }

    return this.getMargin().applySubtractive(box).checkCollision(point);
  }

  protected final Dim getContentBoxX(Dim fullBoxX) {
    return fullBoxX.plus(this.margin.left).plus(this.padding.left);
  }

  protected final Dim getContentBoxY(Dim fullBoxY) {
    return fullBoxY.plus(this.margin.top).plus(this.padding.top);
  }

  protected final Dim getContentBoxWidth(Dim fullBoxWidth) {
    return fullBoxWidth.minus(this.padding.left).minus(this.padding.right).minus(this.margin.left).minus(this.margin.right);
  }

  protected final DimPoint getFullBoxSize(DimPoint contentBoxSize) {
    return new DimPoint(
      contentBoxSize.getX().plus(this.padding.left).plus(this.padding.right).plus(this.margin.left).plus(this.margin.right),
      contentBoxSize.getY().plus(this.padding.top).plus(this.padding.bottom).plus(this.margin.top).plus(this.margin.bottom)
    );
  }

  protected final DimRect getContentBox() {
    return getContentBox(this);
  }

  protected final DimRect getCollisionBox() { return getCollisionBox(this); }

  protected static DimRect getContentBox(IElement element) {
    return element.getPadding().plus(element.getMargin()).applySubtractive(element.getBox());
  }

  protected static DimRect getCollisionBox(IElement element) {
    return element.getMargin().applySubtractive(element.getBox());
  }

  protected static DimRect getFullBox(IElement element) {
    return element.getBox();
  }

  protected final DimRect alignChild(IElement child) {
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
