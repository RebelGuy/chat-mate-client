package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;

import javax.annotation.Nullable;
import java.util.List;

/** An empty element that can be used for easily managing layouts. */
public class EmptyElement extends SingleElement {
  private Dim width;
  private Dim height;

  public EmptyElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.width = ZERO;
    this.height = ZERO;
  }

  public EmptyElement setWidth(Dim width) {
    if (!this.width.equals(width)) {
      this.width = width;
      super.onInvalidateSize();
    }
    return this;
  }

  public EmptyElement setHeight(Dim height) {
    if (!this.height.equals(height)) {
      this.height = height;
      super.onInvalidateSize();
    }
    return this;
  }

  public DimPoint getSize() {
    return super.getFullBoxSize(new DimPoint(this.width, this.height));
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return null;
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    return new DimPoint(Dim.min(this.width, maxContentSize), this.height);
  }

  @Override
  public void renderElement() {
    // nothing to render
  }
}
