package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent.EventPhase;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent.EventType;
import dev.rebel.chatmate.gui.Interactive.Events.ScreenSizeData;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.*;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.util.EnumHelpers;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.rebel.chatmate.gui.Interactive.ElementHelpers.alignElementInBox;
import static dev.rebel.chatmate.util.Objects.firstOrNull;

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
  protected final FontEngine fontEngine;
  protected String name;

  /** Full size. */
  protected DimPoint lastCalculatedSize;
  private DimRect box;
  private RectExtension padding;
  private RectExtension border;
  private RectExtension margin;
  private int zIndex;
  private @Nullable DimRect visibleBox;
  private HorizontalAlignment horizontalAlignment;
  private VerticalAlignment verticalAlignment;
  private SizingMode sizingMode;
  private LayoutGroup layoutGroup;
  private boolean initialised;
  private boolean isHovering;
  protected boolean visible;
  private @Nullable String tooltip;
  private @Nullable Runnable onClick;
  private @Nullable Dim maxWidth;
  private @Nullable Dim maxContentWidth;
  private @Nullable Dim minWidth;
  private @Nullable Dim targetFullHeight;
  private @Nullable Dim targetContentHeight;
  private @Nullable CursorType cursor;
  private List<Runnable> disposers;

  public ElementBase(InteractiveContext context, IElement parent) {
    ID++;
    this.name = String.format("%s-%d", this.getClass().getSimpleName(), ID);
    this.context = context;
    this.parent = parent;
    this.ZERO = context.dimFactory.zeroGui();
    this.fontEngine = context.fontEngine;

    this.box = null;
    this.padding = new RectExtension(context.dimFactory.zeroGui());
    this.border = new RectExtension(context.dimFactory.zeroGui());
    this.margin = new RectExtension(context.dimFactory.zeroGui());
    this.zIndex = 0;
    this.visibleBox = null;
    this.horizontalAlignment = HorizontalAlignment.LEFT;
    this.verticalAlignment = VerticalAlignment.TOP;
    this.sizingMode = SizingMode.ANY;
    this.layoutGroup = LayoutGroup.ALL;
    this.initialised = false;
    this.isHovering = false;
    this.visible = true;

    this.tooltip = null;
    this.onClick = null;
    this.maxWidth = null;
    this.maxContentWidth = null;
    this.minWidth = null;
    this.targetFullHeight = null;
    this.cursor = null;
    this.disposers = new ArrayList<>();
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

  @Override
  public final boolean isInitialised() {
    return this.initialised;
  }

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

  protected boolean isHovering() {
    return this.isHovering;
  }

  /** Called when updating the cursor to decide whether the element should display its custom cursor or not. */
  protected boolean shouldUseCursor() {
    return this.isHovering();
  }

  /** Toggles or untoggles the current cursor, if applicable. */
  protected void updateCursor() {
    if (this.cursor == null || !this.shouldUseCursor()) {
      this.context.cursorService.untoggleCursor(this);
    } else {
      this.context.cursorService.toggleCursor(this.cursor, this, this.getDepth());
    }
  }

  /** Automatically sets the cursor when the mouse pointer is hovering over this element. */
  protected IElement setCursor(@Nullable CursorType cursor) {
    if (this.cursor != cursor) {
      this.cursor = cursor;
      this.updateCursor();
    }

    return this;
  }

  @Override
  public final void onEvent(EventType type, InteractiveEvent<?> event) {
    if (event.getPhase() == EventPhase.TARGET) {
      switch (type) {
        case FOCUS:
          this.onFocus((InteractiveEvent<FocusEventData>)event);
          break;
        case BLUR:
          this.onBlur((InteractiveEvent<FocusEventData>)event);
          break;
        case MOUSE_ENTER:
          this.isHovering = true;
          this.updateCursor();
          this.onMouseEnter((InteractiveEvent<MouseEventData>)event);
          break;
        case MOUSE_EXIT:
          this.isHovering = false;
          this.updateCursor();
          this.onMouseExit((InteractiveEvent<MouseEventData>)event);
          break;
        case WINDOW_RESIZE:
          this.onWindowResize((InteractiveEvent<ScreenSizeData>)event);
          break;
        default:
          throw EnumHelpers.<EventType>assertUnreachable(type);
      }
    } else if (event.getPhase() == EventPhase.CAPTURE) {
      this.onEventCapture(type, event);
    } else if (event.getPhase() == EventPhase.BUBBLE) {
      this.onEventBubble(type, event);
    } else {
      throw EnumHelpers.<EventPhase>assertUnreachable(event.getPhase());
    }
  }

  private void onEventCapture(EventType type, InteractiveEvent<?> event) {
    switch (type) {
      case MOUSE_DOWN:
        this.onCaptureMouseDown((InteractiveEvent<MouseEventData>)event);
        break;
      case MOUSE_MOVE:
        this.onCaptureMouseMove((InteractiveEvent<MouseEventData>)event);
        break;
      case MOUSE_UP:
        this.onCaptureMouseUp((InteractiveEvent<MouseEventData>)event);
        break;
      case MOUSE_SCROLL:
        this.onCaptureMouseScroll((InteractiveEvent<MouseEventData>)event);
        break;
      case MOUSE_ENTER:
        this.onCaptureMouseEnter((InteractiveEvent<MouseEventData>)event);
        break;
      case KEY_DOWN:
        this.onCaptureKeyDown((InteractiveEvent<KeyboardEventData>)event);
        break;
      case KEY_UP:
        this.onCaptureKeyUp((InteractiveEvent<KeyboardEventData>)event);
        break;
      default:
        throw EnumHelpers.<EventType>assertUnreachable(type);
    }
  }

  private void onEventBubble(EventType type, InteractiveEvent<?> event) {
    switch (type) {
      case MOUSE_DOWN:
        if (this.onClick != null && this.onClickHook((InteractiveEvent<MouseEventData>)event)) {
          event.stopPropagation();
          this.onClick.run();
        }
        this.onMouseDown((InteractiveEvent<MouseEventData>)event);
        break;
      case MOUSE_MOVE:
        this.onMouseMove((InteractiveEvent<MouseEventData>)event);
        break;
      case MOUSE_UP:
        this.onMouseUp((InteractiveEvent<MouseEventData>)event);
        break;
      case MOUSE_SCROLL:
        this.onMouseScroll((InteractiveEvent<MouseEventData>)event);
        break;
      case KEY_DOWN:
        this.onKeyDown((InteractiveEvent<KeyboardEventData>)event);
        break;
      case KEY_UP:
        this.onKeyUp((InteractiveEvent<KeyboardEventData>)event);
        break;
      default:
        throw EnumHelpers.<EventType>assertUnreachable(type);
    }
  }

  public void onMouseDown(InteractiveEvent<MouseEventData> e) {}
  public void onCaptureMouseDown(InteractiveEvent<MouseEventData> e) {}
  public void onMouseMove(InteractiveEvent<MouseEventData> e) {}
  public void onCaptureMouseMove(InteractiveEvent<MouseEventData> e) {}
  public void onMouseUp(InteractiveEvent<MouseEventData> e) {}
  public void onCaptureMouseUp(InteractiveEvent<MouseEventData> e) {}
  public void onMouseScroll(InteractiveEvent<MouseEventData> e) {}
  public void onCaptureMouseScroll(InteractiveEvent<MouseEventData> e) {}
  public void onKeyDown(InteractiveEvent<KeyboardEventData> e) {}
  public void onCaptureKeyDown(InteractiveEvent<KeyboardEventData> e) {}
  public void onKeyUp(InteractiveEvent<KeyboardEventData> e) {}
  public void onCaptureKeyUp(InteractiveEvent<KeyboardEventData> e) {}
  public void onFocus(InteractiveEvent<FocusEventData> e) {}
  public void onBlur(InteractiveEvent<FocusEventData> e) {}
  /** Target-only - this cannot be cancelled. */
  public void onMouseEnter(InteractiveEvent<MouseEventData> e) {}
  /** The onCaptureMouseEnter event is special - if cancelled, all downstream elements to which we didn't get to yet
   * will receive the MOUSE_EXIT event. */
  public void onCaptureMouseEnter(InteractiveEvent<MouseEventData> e) {}
  /** This doesn't bubble, it is target-only. there is no way to cancel this. */
  public void onMouseExit(InteractiveEvent<MouseEventData> e) {}
  public void onWindowResize(InteractiveEvent<ScreenSizeData> e) {}

  @Override
  public final void onCloseScreen() { this.parent.onCloseScreen(); }

  // todo: move this into the context as a callback method instead. it's confusing that this is a mandatory bubble-up callback
  @Override
  public final void onInvalidateSize() {
    this.parent.onInvalidateSize();
  }

  @Override
  public void onDisposed() {
    this.parent = null;
    this.disposers.forEach(Runnable::run);

    if (this.getChildren() != null) {
      this.getChildren().forEach(IElement::onDisposed);
    }
  }

  /** The disposer will be called when this element is disposed. */
  protected void addDisposer(Runnable disposer) {
    this.disposers.add(disposer);
  }

  /** Called when a critical error occurs. Only do clean-up logic if overriding this. No guarantee is made about the initialisation of state. */
  public void onError() {}

  /** Called when the user clicks the element and an onClick handler exists. Return true to allow the click (default), or false to block the click.
   * If returning true, the onClick handler is guaranteed to be called. */
  protected boolean onClickHook(InteractiveEvent<MouseEventData> e) {
    return true;
  }

  /** Should return the full size. Do NOT call this method in the context of `super` or `this`, only on other elements. Instead, call `this.onCalculateSize`. */
  @Override
  public final DimPoint calculateSize(Dim maxFullWidth) {
    initialiseIfRequired();

    try {
      if (this.maxWidth != null) {
        maxFullWidth = Dim.min(this.maxWidth, maxFullWidth);
      }

      // add a wrapper around the calculation method so we can cache the calculated size and provide a context for working
      // with content units (rather than full units).
      Dim contentWidth = getContentBoxWidth(maxFullWidth);
      if (this.maxContentWidth != null) {
        contentWidth = Dim.min(this.maxContentWidth, contentWidth);
      }
      DimPoint size = this.calculateThisSize(contentWidth);
      DimPoint fullSize = getFullBoxSize(size);
      if (this.targetFullHeight != null) {
        // overwrite the calculated height with the target height
        // todo: this is fine, but elements would need some sort of overflow handling in the future (e.g. overflow, hide, scroll)
        // todo: there should also be an implicit max height here to prevent contents from overflowing off the main window
        fullSize = new DimPoint(fullSize.getX(), this.targetFullHeight);
      } else if (this.targetContentHeight != null) {
        fullSize = new DimPoint(fullSize.getX(), getFullBoxHeight(this.targetContentHeight));
      }
      this.lastCalculatedSize = fullSize;
      return fullSize;
    } catch (Exception e) {
      this.onError();
      throw e;
    }
  }

  /** Should return the content size. Call this method ONLY in the context of `super` or `this`. For other elements, call `element.calculateSize()`. */
  protected abstract DimPoint calculateThisSize(Dim maxContentSize);

  @Override
  public final DimPoint getLastCalculatedSize() {
    return this.lastCalculatedSize;
  }

  public final DimPoint getLastCalculatedSizeOrZero() {
    return this.lastCalculatedSize != null ? this.lastCalculatedSize : new DimPoint(gui(0), gui(0));
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
  public final void render(@Nullable Consumer<Runnable> renderContextWrapper) {
    initialiseIfRequired();
    if (!this.visible) {
      return;
    }

    // what a mess... thanks java
    Consumer<Runnable> contextWrapper = renderContextWrapper == null ? Runnable::run : renderContextWrapper;
    @Nullable DimRect visibleBox = this.getVisibleBox();
    Consumer<Runnable> wrapperWithScissor;
    if (visibleBox != null) {
      wrapperWithScissor = (onRender) -> RendererHelpers.withScissor(visibleBox, this.context.dimFactory.getMinecraftSize(), () -> contextWrapper.accept(onRender));
    } else {
      wrapperWithScissor = contextWrapper;
    }

    this.context.renderer.render(this, () -> {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableLighting();
      wrapperWithScissor.accept(this::renderElementSafe);
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
    });
  }

  private void renderElementSafe() {
    try {
      this.renderElement();
    } catch (Exception e) {
      context.logService.logError(this, this.name, "encountered an error during rendering:", e);
    }
  }

  /** You should never call super.render() from this element, as it will cause an infinite loop.
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
  public final IElement setOnClick(@Nullable Runnable onClick) {
    if (this.onClick != onClick) {
      this.onClick = onClick;
      this.setCursor(CursorType.CLICK);
    }
    return this;
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
  public int getDepth() {
    return this.parent.getDepth() + 1;
  }

  @Override
  public @Nullable DimRect getVisibleBox() {
    @Nullable DimRect parentVisibleBox = this.parent.getVisibleBox();
    if (parentVisibleBox == null) {
      return this.visibleBox;
    } else if (parentVisibleBox != null && this.visibleBox == null) {
      return parentVisibleBox;
    } else {
      DimRect effectiveVisibleRect = parentVisibleBox.clamp(this.visibleBox);

      if (effectiveVisibleRect.getWidth().lte(ZERO) || effectiveVisibleRect.getHeight().lte(ZERO)) {
        // the rects don't overlap - nothing will render
        return this.visibleBox.withSize(new DimPoint(ZERO, ZERO));
      } else {
        return effectiveVisibleRect;
      }
    }
  }

  @Override
  public IElement setVisibleBox(@Nullable DimRect visibleBox) {
    if (!Objects.equals(this.visibleBox, visibleBox)) {
      this.visibleBox = visibleBox;

      // the visible boxes don't directly relate to the layout of elements, but it is still possible that some layout mechanism depends on this box - just in case, recalculate sizes
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
  public IElement setLayoutGroup(LayoutGroup layoutGroup) {
    if (this.layoutGroup != layoutGroup) {
      this.layoutGroup = layoutGroup;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public LayoutGroup getLayoutGroup() {
    return this.layoutGroup;
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
  public IElement setMaxContentWidth(@Nullable Dim maxContentWidth) {
    if (!Objects.equals(this.maxContentWidth, maxContentWidth)) {
      this.maxContentWidth = maxContentWidth;
      this.onInvalidateSize();
    }
    return this;
  }

  public @Nullable Dim getMaxContentWidth() {
    return this.maxContentWidth;
  }

  @Override
  public IElement setMinWidth(@Nullable Dim minWith) {
    if (!Objects.equals(this.minWidth, minWith)) {
      this.minWidth = minWith;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public @Nullable Dim getMinWidth() {
    return this.minWidth;
  }

  @Override
  public IElement setTargetHeight(@Nullable Dim targetHeight) {
    if (!Objects.equals(this.targetFullHeight, targetHeight)) {
      this.targetFullHeight = targetHeight;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public @Nullable Dim getTargetHeight() {
    if (this.targetFullHeight != null) {
      return this.targetFullHeight;
    } else if (this.targetContentHeight != null) {
      return getFullBoxHeight(this.targetContentHeight);
    } else {
      return null;
    }
  }
// set target content height, not target height, that way our padding won't affect the result
  @Override
  public @Nullable Dim getEffectiveTargetHeight() {
    @Nullable Dim parentHeight = this.parent.getEffectiveTargetHeight();
    if (parentHeight != null) {
      parentHeight = parentHeight.minus(this.parent.getPadding().getExtendedHeight()).minus(this.parent.getBorder().getExtendedHeight()).minus(this.parent.getMargin().getExtendedHeight());
    }

    if (this.getTargetHeight() == null) {
      return parentHeight;
    } else if (parentHeight == null) {
      return this.getTargetHeight();
    } else {
      return Dim.min(this.getTargetHeight(), parentHeight);
    }
  }

  /** If `targetHeight` (i.e. the *full* height) is set for this element, it will take precedence over the content height. */
  @Override
  public IElement setTargetContentHeight(@Nullable Dim targetContentHeight) {
    if (!Objects.equals(this.targetContentHeight, targetContentHeight)) {
      this.targetContentHeight = targetContentHeight;
      this.onInvalidateSize();
    }
    return this;
  }

  @Override
  public @Nullable Dim getTargetContentHeight() {
    @Nullable Dim targetFullHeight = this.getTargetHeight();
    return targetFullHeight != null ? getContentBoxHeight(targetFullHeight) : null;
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

  protected final Dim getContentBoxHeight(Dim fullBoxHeight) {
    return this.getContentBoxSize(new DimPoint(ZERO, fullBoxHeight)).getY();
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

  protected final Dim getFullBoxHeight(Dim contentBoxHeight) {
    return this.getFullBoxSize(new DimPoint(ZERO, contentBoxHeight)).getY();
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
    @Nullable DimRect visibleBox = element.getVisibleBox();
    DimRect collisionBox = getBorderBox(element);
    if (visibleBox == null) {
      return collisionBox;
    } else {
      return collisionBox.clamp(visibleBox);
    }
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
