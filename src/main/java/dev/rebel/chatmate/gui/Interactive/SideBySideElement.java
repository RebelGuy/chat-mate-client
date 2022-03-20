package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Guarantees to render all children elements on a single line. */
public class SideBySideElement extends ContainerElement {
  private Dim elementPadding;
  private Map<IElement, Float> elementBiases;

  public SideBySideElement(InteractiveContext context, IElement parent) {
    super(context, parent, LayoutMode.INLINE);

    this.elementPadding = context.dimFactory.fromGui(5);
    this.elementBiases = new HashMap<>();
  }

  public SideBySideElement setElementPadding(Dim elementPadding) {
    this.elementPadding = elementPadding;
    return this;
  }

  public SideBySideElement addElement(float bias, IElement element) {
    element.setSizingMode(SizingMode.FILL);
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

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    // override default container layout engine.
    // we must ensure that everything fits on a single line.
    float totalBias = Collections.eliminate(this.elementBiases.values(), Float::sum);
    Dim totalPadding = this.elementPadding.times(this.elementBiases.size() - 1);
    Dim availableWidth = maxContentSize.minus(totalPadding);

    Dim containerHeight = ZERO;

    // first pass - get sizes and position relatively
    Dim currentX = ZERO;
    for (IElement element : this.children) {
      Dim elementMaxWidth = availableWidth.times(this.elementBiases.get(element) / totalBias);
      DimPoint size = element.calculateSize(elementMaxWidth);

      DimPoint position = new DimPoint(currentX, ZERO);
      this.childrenRelBoxes.put(element, new DimRect(position, size));

      currentX = currentX.plus(elementMaxWidth.plus(this.elementPadding));
      containerHeight = Dim.max(containerHeight, size.getY());
    }

    // second pass - align correctly in available space
    for (IElement element : this.children) {
      Dim elementMaxWidth = availableWidth.times(this.elementBiases.get(element) / totalBias);
      DimRect availableBox = new DimRect(ZERO, ZERO, elementMaxWidth, containerHeight);

      DimRect relBox = this.childrenRelBoxes.get(element);
      DimPoint modifiedPosition = ElementHelpers.alignElementInBox(relBox.getSize(), availableBox, element.getHorizontalAlignment(), element.getVerticalAlignment()).getPosition();
      this.childrenRelBoxes.put(element, relBox.withTranslation(modifiedPosition));
    }

    return new DimPoint(maxContentSize, containerHeight);
  }
}
