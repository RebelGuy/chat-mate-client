package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

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

  public ElementReference setUnderlyingElement(IElement element) {
    if (element != null) {
      element.setParent(this);
    }
    this.underlyingElement = element;
    this.parent.onInvalidateSize();
    return this;
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
  public void onCreate() {
    if (this.underlyingElement != null) {
      this.underlyingElement.onCreate();
    }
  }

  @Override
  public void onDispose() {
    if (this.underlyingElement != null) {
      this.underlyingElement.onDispose();
    }
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
  public void render() {
    if (this.underlyingElement != null) {
      this.underlyingElement.render();
    }

    if (this.context.debugElement == this) {
      ElementHelpers.renderDebugInfo(this, this.context);
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
  public IElement setZIndex(int zIndex) {
    return this.underlyingElement == null ? this : this.underlyingElement.setZIndex(zIndex);
  }

  @Override
  public Layout.HorizontalAlignment getHorizontalAlignment() {
    return this.underlyingElement == null ? Layout.HorizontalAlignment.LEFT : this.underlyingElement.getHorizontalAlignment();
  }

  @Override
  public IElement setHorizontalAlignment(Layout.HorizontalAlignment horizontalAlignment) {
    return this.underlyingElement == null ? this : this.underlyingElement.setHorizontalAlignment(horizontalAlignment);
  }

  @Override
  public Layout.VerticalAlignment getVerticalAlignment() {
    return this.underlyingElement == null ? Layout.VerticalAlignment.TOP : this.underlyingElement.getVerticalAlignment();
  }

  @Override
  public IElement setVerticalAlignment(Layout.VerticalAlignment verticalAlignment) {
    return this.underlyingElement == null ? this : this.underlyingElement.setVerticalAlignment(verticalAlignment);
  }

  @Override
  public Layout.SizingMode getSizingMode() {
    return this.underlyingElement == null ? Layout.SizingMode.ANY : this.underlyingElement.getSizingMode();
  }

  @Override
  public IElement setSizingMode(Layout.SizingMode sizingMode) {
    return this.underlyingElement == null ? this : this.underlyingElement.setSizingMode(sizingMode);
  }
}
