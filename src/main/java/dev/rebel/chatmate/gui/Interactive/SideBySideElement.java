package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Guarantees to render all children elements on a single line. It is the horizontal version of the `ListElement`. */
public class SideBySideElement extends ContainerElement {
  private Dim elementPadding;
  private Map<IElement, Float> elementBiases;

  public SideBySideElement(InteractiveContext context, IElement parent) {
    super(context, parent, LayoutMode.INLINE);

    this.elementPadding = context.dimFactory.fromGui(5);
    this.elementBiases = new HashMap<>();
  }

  public SideBySideElement setElementPadding(Dim elementPadding) {
    if (!this.elementPadding.equals(elementPadding)) {
      this.elementPadding = elementPadding;

      // update the width of all padding elements
      Collections.map(
          Collections.filter(super.children, el -> el instanceof EmptyElement),
          el -> ((EmptyElement)el).setWidth(elementPadding)
      );

      super.onInvalidateSize();
    }
    return this;
  }

  public SideBySideElement addElement(float bias, IElement element) {
    element.setSizingMode(SizingMode.FILL);

    // to be able to use the `super` layout implementation that respects each of the children's layout modes, we add
    // empty elements for padding horizontally.
    if (super.children.size() > 0) {
      super.addElement(new EmptyElement(super.context, this).setWidth(this.elementPadding));
    }

    this.elementBiases.put(element, bias);
    super.addElement(element);
    return this;
  }

  public SideBySideElement removeElement(IElement element) {
    this.elementBiases.remove(element);
    super.removeElement(element);
    return this;
  }

  public SideBySideElement setBias(IElement element, float bias) {
    if (this.elementBiases.containsKey(element)) {
        this.elementBiases.put(element, bias);
    }
    return this;
  }

  /** Has no effect - all SideBySide elements use the FILL sizing mode. */
  @Override
  public IElement setSizingMode(SizingMode sizingMode) {
    return this;
  }

  @Override
  public SizingMode getSizingMode() {
    return SizingMode.FILL;
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    // ensure everything fits on a single line
    float totalBias = Collections.eliminate(this.elementBiases.values(), Float::sum);
    Dim totalPadding = this.elementPadding.times(this.elementBiases.size() - 1);
    Dim availableWidth = maxContentSize.minus(totalPadding);

    List<Tuple2<IElement, DimPoint>> elementSizes = new ArrayList<>();
    for (IElement element : super.children) {
      if (element instanceof EmptyElement) {
        elementSizes.add(new Tuple2<>(element, element.<EmptyElement>cast().getSize()));
        continue;
      }

      if (!this.elementBiases.containsKey(element)) {
        continue;
      }

      Dim elementMaxWidth = availableWidth.times(this.elementBiases.get(element) / totalBias);
      DimPoint size = new DimPoint(elementMaxWidth, element.calculateSize(elementMaxWidth).getY());
      elementSizes.add(new Tuple2<>(element, size));
    }

    return super.calculateInlineSize(elementSizes, maxContentSize);
  }
}
