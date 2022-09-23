package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.LayoutGroup;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** An element that acts as a reference for a dynamic child. This means the reference has to be only instantiated once
 * (for example for the purposes of initialising layout lists), but its contents can be freely swapped out.
 * Applying layout settings to this element if the underlying element is not set has no effect. */
public class ElementReference implements IElement {
  private final InteractiveScreen.InteractiveContext context;
  private final Dim ZERO;
  private final DimPoint ZERO_POINT;
  private final DimRect ZERO_RECT;
  private final Layout.RectExtension ZERO_EXT;

  private IElement parent;
  private @Nullable IElement underlyingElement;

  public ElementReference(InteractiveScreen.InteractiveContext context, IElement parent) {
    this.context = context;
    this.parent = parent;
    this.ZERO = context.dimFactory.zeroGui();
    this.ZERO_POINT = new DimPoint(ZERO, ZERO);
    this.ZERO_RECT = new DimRect(ZERO_POINT, ZERO_POINT);
    this.ZERO_EXT = new Layout.RectExtension(ZERO);

    this.underlyingElement = null;
  }

  /** Schedules the provided element to be set. Note that the element will not be updated until the current render cycle is complete.
   * When accessing this method from the main thread, and outside the render() method, the new element can be assumed to have been set immediately. */
  public ElementReference setUnderlyingElement(IElement element) {
    if (this.underlyingElement == element) {
      return this;
    }

    // we don't set the underlying element here directly because it is entirely possible that the underlying element is set on another thread,
    // so just tell the InteractiveScreen to recalculate layouts, and update the underlying element later
    this.context.renderer.runSideEffect(() -> {
      this.underlyingElement = element;

      if (this.underlyingElement != null) {
        this.underlyingElement.setParent(this.parent);
      }

      this.parent.onInvalidateSize();
    });

    return this;
  }

  public @Nullable IElement getUnderlyingElement() {
    return this.underlyingElement;
  }

  public boolean compareWithUnderlyingElement(IElement element) {
    return Objects.equals(this.underlyingElement, element);
  }

  @Override
  public IElement getParent() {
    return this.parent;
  }

  @Override
  public IElement setParent(IElement parent) {
    this.parent = parent;
    return this;
  }

  @Nullable
  @Override
  public List<IElement> getChildren() {
    return this.underlyingElement == null ? null : Collections.list(this.underlyingElement);
  }

  @Override
  public void onInitialise() {
    // note that, realistically, this will never be called. we don't want to relay this to the underlying element, though,
    // because we have no way of checking whether it has been initialised already. leave it up to its default ElementBase
    // implementation to handle.
  }

  @Override
  public void onEvent(Events.EventType type, Events.IEvent<?> event) {
    if (this.underlyingElement != null) {
      this.underlyingElement.onEvent(type, event);
    }
  }

  @Override
  public void onCloseScreen() {
    this.parent.onCloseScreen();
  }

  @Override
  public void onInvalidateSize() {
    this.parent.onInvalidateSize();
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    return this.underlyingElement == null ? ZERO_POINT : this.underlyingElement.calculateSize(maxWidth);
  }

  @Override
  public DimPoint getLastCalculatedSize() {
    return this.underlyingElement == null ? ZERO_POINT : this.underlyingElement.getLastCalculatedSize();
  }

  @Override
  public void setBox(DimRect box) {
    if (this.underlyingElement != null) {
      this.underlyingElement.setBox(box);
    }
  }

  @Override
  public DimRect getBox() {
    return this.underlyingElement == null ? ZERO_RECT : this.underlyingElement.getBox();
  }

  @Override
  public void render(@Nullable Consumer<Runnable> renderContextWrapper) {
    if (this.underlyingElement != null) {
      this.underlyingElement.render(renderContextWrapper);
    }

    if (this.context.debugElement == this && this.underlyingElement != null) {
      this.context.debugElement = this.underlyingElement;
    }
  }

  @Override
  public boolean getVisible() {
    return this.underlyingElement == null ? false : this.underlyingElement.getVisible();
  }

  @Override
  public IElement setVisible(boolean visible) {
    return this.underlyingElement == null ? this : this.underlyingElement.setVisible(visible);
  }

  @Override
  public Layout.RectExtension getPadding() {
    return this.underlyingElement == null ? ZERO_EXT : this.underlyingElement.getPadding();
  }

  @Override
  public IElement setPadding(Layout.RectExtension padding) {
    return this.underlyingElement == null ? this : this.underlyingElement.setPadding(padding);
  }

  @Override
  public Layout.RectExtension getBorder() {
    return this.underlyingElement == null ? ZERO_EXT : this.underlyingElement.getBorder();
  }

  @Override
  public IElement setBorder(Layout.RectExtension border) {
    return this.underlyingElement == null ? this : this.underlyingElement.setBorder(border);
  }

  @Override
  public Layout.RectExtension getMargin() {
    return this.underlyingElement == null ? ZERO_EXT : this.underlyingElement.getMargin();
  }

  @Override
  public IElement setMargin(Layout.RectExtension margin) {
    return this.underlyingElement == null ? this : this.underlyingElement.setMargin(margin);
  }

  @Override
  public int getZIndex() {
    return this.underlyingElement == null ? 0 : this.underlyingElement.getZIndex();
  }

  @Override
  public int getEffectiveZIndex() {
    return this.underlyingElement == null ? this.parent.getEffectiveZIndex() : this.underlyingElement.getEffectiveZIndex();
  }

  @Override
  public IElement setZIndex(int zIndex) {
    return this.underlyingElement == null ? this : this.underlyingElement.setZIndex(zIndex);
  }

  @Override
  public @Nullable DimRect getVisibleBox() {
    return this.underlyingElement == null ? null : this.underlyingElement.getVisibleBox();
  }

  @Override
  public IElement setVisibleBox(@Nullable DimRect visibleBox) {
    return this.underlyingElement == null ? this : this.underlyingElement.setVisibleBox(visibleBox);
  }

  @Override
  public HorizontalAlignment getHorizontalAlignment() {
    return this.underlyingElement == null ? HorizontalAlignment.LEFT : this.underlyingElement.getHorizontalAlignment();
  }

  @Override
  public IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
    return this.underlyingElement == null ? this : this.underlyingElement.setHorizontalAlignment(horizontalAlignment);
  }

  @Override
  public VerticalAlignment getVerticalAlignment() {
    return this.underlyingElement == null ? VerticalAlignment.TOP : this.underlyingElement.getVerticalAlignment();
  }

  @Override
  public IElement setVerticalAlignment(VerticalAlignment verticalAlignment) {
    return this.underlyingElement == null ? this : this.underlyingElement.setVerticalAlignment(verticalAlignment);
  }

  @Override
  public SizingMode getSizingMode() {
    return this.underlyingElement == null ? SizingMode.ANY : this.underlyingElement.getSizingMode();
  }

  @Override
  public IElement setSizingMode(SizingMode sizingMode) {
    return this.underlyingElement == null ? this : this.underlyingElement.setSizingMode(sizingMode);
  }

  @Override
  public LayoutGroup getLayoutGroup() {
    return this.underlyingElement == null ? LayoutGroup.ALL : this.underlyingElement.getLayoutGroup();
  }

  @Override
  public IElement setLayoutGroup(LayoutGroup layoutGroup) {
    return this.underlyingElement == null ? this : this.underlyingElement.setLayoutGroup(layoutGroup);
  }

  @Override
  public @Nullable String getTooltip() {
    return this.underlyingElement == null ? null : this.underlyingElement.getTooltip();
  }

  @Override
  public IElement setTooltip(@Nullable String text) {
    return this.underlyingElement == null ? this : this.underlyingElement.setTooltip(text);
  }

  @Override
  public IElement setName(String name) {
    return this.underlyingElement == null ? this : this.underlyingElement.setName(name);
  }

  @Override
  public IElement setMaxWidth(@Nullable Dim maxWidth) {
    return this.underlyingElement == null ? this : this.underlyingElement.setMaxWidth(maxWidth);
  }

  @Override
  public IElement setMaxContentWidth(@Nullable Dim maxContentWidth) {
    return this.underlyingElement == null ? this : this.underlyingElement.setMaxWidth(maxContentWidth);
  }

  @Override
  public IElement setTargetHeight(@Nullable Dim height) {
    return this.underlyingElement == null ? this : this.underlyingElement.setTargetHeight(height);
  }

  @Override
  public IElement setTargetContentHeight(@Nullable Dim height) {
    return this.underlyingElement == null ? this : this.underlyingElement.setTargetContentHeight(height);
  }

  @Override
  public @Nullable Dim getTargetHeight() {
    return this.underlyingElement == null ? null : this.underlyingElement.getTargetHeight();
  }

  @Override
  public @Nullable Dim getTargetContentHeight() {
    return this.underlyingElement == null ? null : this.underlyingElement.getTargetContentHeight();
  }

  @Override
  public <T extends IElement> T cast() {
    return this.underlyingElement == null ? null : (T)this.underlyingElement;
  }
}
