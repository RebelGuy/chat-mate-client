package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ScreenRenderer;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.StateManagement.AnimatedBool;
import dev.rebel.chatmate.gui.StateManagement.State;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;

import java.util.Objects;

import static dev.rebel.chatmate.gui.Interactive.ElementHelpers.alignElementInBox;

// a note about box size terminology:
// the full box includes contents, surrounded by padding, surrounded by border, surrounded by margin. it is the box used when calculating any sort of layout.
// the collision box includes contents, surrounded by padding, surrounded by border. it is the box used when checking for mouse pointer collisions with this element.
// the padding box includes contents, surrounded by padding. it is used to separate the contents from the border (though an element may not necessarily draw a physical border).
// the content box includes contents only. this is where the element's contents are rendered into.

public abstract class ElementBase implements IElement {
  private static int ID = 0;

  protected final InteractiveContext context;
  protected IElement parent;
  protected final Dim ZERO;
  protected final FontRenderer font;
  protected String name;

  protected DimPoint lastCalculatedSize;
  private DimRect box;
  private RectExtension padding;
  private RectExtension border;
  private RectExtension margin;
  private int zIndex;
  private HorizontalAlignment horizontalAlignment;
  private VerticalAlignment verticalAlignment;
  private SizingMode sizingMode;
  private boolean initialised;
  protected boolean visible;
  private @Nullable String tooltip;
  private @Nullable Dim maxWidth;

  public ElementBase(InteractiveContext context, IElement parent) {
    ID++;
    this.name = String.format("%s-%d", this.getClass().getSimpleName(), ID);
    this.context = context;
    this.parent = parent;
    this.ZERO = context.dimFactory.zeroGui();
    this.font = context.fontRenderer;

    this.box = null;
    this.padding = new RectExtension(context.dimFactory.zeroGui());
    this.border = new RectExtension(context.dimFactory.zeroGui());
    this.margin = new RectExtension(context.dimFactory.zeroGui());
    this.zIndex = 0;
    this.horizontalAlignment = HorizontalAlignment.LEFT;
    this.verticalAlignment = VerticalAlignment.TOP;
    this.sizingMode = SizingMode.ANY;
    this.initialised = false;
    this.visible = true;

    this.tooltip = null;
    this.maxWidth = null;
  }

  @Override
  public final IElement getParent() { return this.parent; }

  @Override
  public IElement setParent(IElement parent) {
    this.parent = parent;
    return this;
  }

  @Override
  public void onInitialise() { }

  protected final boolean isInitialised() { return this.initialised; }

  @Override
  public boolean getVisible() {
    return this.visible;
  }

  @Override
  public IElement setVisible(boolean visible) {
    if (this.visible != visible) {
      this.visible = visible;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public final void onEvent(EventType type, IEvent<?> event) {
    if (event.getPhase() == EventPhase.TARGET) {
      switch (type) {
        case FOCUS:
          this.onFocus((IEvent<FocusEventData>)event);
          break;
        case BLUR:
          this.onBlur((IEvent<FocusEventData>)event);
          break;
        case MOUSE_ENTER:
          this.onMouseEnter((IEvent<MouseEventData.In>)event);
          break;
        case MOUSE_EXIT:
          this.onMouseExit((IEvent<MouseEventData.In>)event);
          break;
        case WINDOW_RESIZE:
          this.onWindowResize((IEvent<SizeData>)event);
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
      case MOUSE_ENTER:
        this.onCaptureMouseEnter((IEvent<MouseEventData.In>)event);
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
  /** Target-only - this cannot be cancelled. */
  public void onMouseEnter(IEvent<MouseEventData.In> e) {}
  /** The onCaptureMouseEnter event is special - if cancelled, all downstream elements to which we didn't get to yet
   * will receive the MOUSE_EXIT event. */
  public void onCaptureMouseEnter(IEvent<MouseEventData.In> e) {}
  /** This doesn't bubble, it is target-only. there is no way to cancel this. */
  public void onMouseExit(IEvent<MouseEventData.In> e) {}
  public void onWindowResize(IEvent<SizeData> e) {}

  @Override
  public final void onCloseScreen() { this.parent.onCloseScreen(); }

  // todo: move this into the context as a callback method instead. it's confusing that this is a mandatory bubble-up callback
  @Override
  public final void onInvalidateSize() {
    this.parent.onInvalidateSize();
  }

  /** Should return the full size. Do NOT call this method in the context of `super` or `this`, only on other elements. Instead, call `this.onCalculateSize`. */
  @Override
  public final DimPoint calculateSize(Dim maxFullWidth) {
    initialiseIfRequired();

    if (this.maxWidth != null) {
      maxFullWidth = Dim.min(this.maxWidth, maxFullWidth);
    }

    // add a wrapper around the calculation method so we can cache the calculated size and provide a context for working
    // with content units (rather than full units).
    Dim contentWidth = getContentBoxWidth(maxFullWidth);
    DimPoint size = this.calculateThisSize(contentWidth);
    DimPoint fullSize = getFullBoxSize(size);
    this.lastCalculatedSize = fullSize;
    return fullSize;
  }

  /** Should return the content size. Call this method ONLY in the context of `super` or `this`. For other elements, call `element.calculateSize()`. */
  protected abstract DimPoint calculateThisSize(Dim maxContentSize);

  @Override
  public final DimPoint getLastCalculatedSize() {
    return this.lastCalculatedSize;
  }

  // todo: ideally this should be final, but an element might want to hook into this so they can deal with the new rect in some way.
  // since it is easy to forget to call `super.setBox`, make a onBoxSet() virtual method instead.
  @Override
  public void setBox(DimRect box) {
    this.box = box;
  }

  /** Directly sets the underlying box of the element, bypassing any hooks. */
  protected final void setBoxUnsafe(DimRect box) {
    this.box = box;
  }

  @Override
  public final DimRect getBox() {
    return this.box;
  }

  @Override
  public final void render() {
    initialiseIfRequired();
    if (!this.visible) {
      return;
    }

    this.context.renderer.render(this, () -> {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableLighting();
      this.renderElement();
      GlStateManager.popMatrix();
    });
  }

  /** You should never call super.render() from this method, as it will cause an infinite loop.
   * If you need to render a base element, use super.renderElement() instead. */
  protected abstract void renderElement();

  private void initialiseIfRequired() {
    if (!this.initialised) {
      this.initialised = true;
      this.onInitialise();
    }
  }

  @Override
  public final IElement setTooltip(@Nullable String text) {
    this.tooltip = text;
    return this;
  }

  @Override
  public final @Nullable String getTooltip() {
    return this.tooltip;
  }

  @Override
  public final IElement setPadding(RectExtension padding) {
    if (!this.padding.equals(padding)) {
      this.padding = padding;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public final RectExtension getPadding() {
    return this.padding == null ? new RectExtension(this.context.dimFactory.zeroGui()) : this.padding;
  }

  @Override
  public final IElement setBorder(RectExtension border) {
    if (!this.border.equals(border)) {
      this.border = border;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public final RectExtension getBorder() {
    return this.border == null ? new RectExtension(this.context.dimFactory.zeroGui()) : this.border;
  }

  @Override
  public final IElement setMargin(RectExtension margin) {
    if (!this.margin.equals(margin)) {
      this.margin = margin;
      this.onInvalidateSize();
    }
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
  public final int getEffectiveZIndex() {
    return this.zIndex + this.parent.getEffectiveZIndex();
  }

  @Override
  public final IElement setZIndex(int zIndex) {
    if (this.zIndex != zIndex) {
      this.zIndex = zIndex;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public final IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
    if (this.horizontalAlignment != horizontalAlignment) {
      this.horizontalAlignment = horizontalAlignment;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public final HorizontalAlignment getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public final IElement setVerticalAlignment(VerticalAlignment verticalAlignment) {
    if (this.verticalAlignment != verticalAlignment) {
      this.verticalAlignment = verticalAlignment;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public final VerticalAlignment getVerticalAlignment() {
    return this.verticalAlignment;
  }

  @Override
  public IElement setSizingMode(SizingMode sizingMode) {
    if (this.sizingMode != sizingMode) {
      this.sizingMode = sizingMode;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public SizingMode getSizingMode() {
    return this.sizingMode;
  }

  @Override
  public IElement setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public IElement setMaxWidth(@Nullable Dim maxWidth) {
    if (!Objects.equals(this.maxWidth, maxWidth)) {
      this.maxWidth = maxWidth;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public final <T extends IElement> T cast() {
    return (T)this;
  }

  protected final DimPoint getContentBoxSize(DimPoint fullBoxSize) {
    return new DimPoint(
      fullBoxSize.getX().minus(this.getPadding().getExtendedWidth()).minus(this.getBorder().getExtendedWidth()).minus(this.getMargin().getExtendedWidth()),
      fullBoxSize.getY().minus(this.getPadding().getExtendedHeight()).minus(this.getBorder().getExtendedHeight()).minus(this.getMargin().getExtendedHeight())
    );
  }

  protected final Dim getContentBoxWidth(Dim fullBoxWidth) {
    return this.getContentBoxSize(new DimPoint(fullBoxWidth, ZERO)).getX();
  }

  protected final DimPoint getFullBoxSize(DimPoint contentBoxSize) {
    return new DimPoint(
      contentBoxSize.getX().plus(this.getPadding().getExtendedWidth()).plus(this.getBorder().getExtendedWidth()).plus(this.getMargin().getExtendedWidth()),
      contentBoxSize.getY().plus(this.getPadding().getExtendedHeight()).plus(this.getBorder().getExtendedHeight()).plus(this.getMargin().getExtendedHeight())
    );
  }

  protected final Dim getFullBoxWidth(Dim contentBoxWidth) {
    return this.getFullBoxSize(new DimPoint(contentBoxWidth, ZERO)).getX();
  }

  protected final DimRect getContentBox() {
    return getContentBox(this);
  }

  protected final DimRect getCollisionBox() { return getCollisionBox(this); }

  protected final DimRect getBorderBox() { return getBorderBox(this); }

  protected final DimRect getPaddingBox() { return getPaddingBox(this); }

  protected static DimRect getContentBox(IElement element) {
    return element.getPadding().plus(element.getBorder()).plus(element.getMargin()).applySubtractive(element.getBox());
  }

  protected static DimRect getPaddingBox(IElement element) {
    return element.getMargin().plus(element.getBorder()).applySubtractive(element.getBox());
  }

  protected static DimRect getBorderBox(IElement element) {
    return element.getMargin().applySubtractive(element.getBox());
  }

  protected static DimRect getCollisionBox(IElement element) {
    return getBorderBox(element);
  }

  protected static DimRect getFullBox(IElement element) {
    return element.getBox();
  }

  protected final DimRect alignChild(IElement child) {
    return alignElementInBox(child.getLastCalculatedSize(), this.getContentBox(), child.getHorizontalAlignment(), child.getVerticalAlignment());
  }

  protected final Dim gui(float guiValue) {
    return this.context.dimFactory.fromGui(guiValue);
  }

  protected final Dim screen(float screenValue) {
    return this.context.dimFactory.fromScreen(screenValue);
  }
}
