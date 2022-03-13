package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
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

  public SideBySideElement addElement(IElement element, float bias) {
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
  public DimPoint calculateSize(Dim maxWidth) {
    // override default container layout engine.
    // we must ensure that everything fits on a single line.
    maxWidth = this.getContentBoxWidth(maxWidth);

    float totalBias = Collections.eliminate(this.elementBiases.values(), Float::sum);
    Dim totalPadding = this.elementPadding.times(this.elementBiases.size() - 1);
    Dim availableWidth = maxWidth.minus(totalPadding);

    Dim containerHeight = ZERO;

    Dim currentX = ZERO;
    for (IElement element : this.children) {
      Dim elementMaxWidth = availableWidth.times(this.elementBiases.get(element) / totalBias);
      DimPoint size = element.calculateSize(elementMaxWidth);

      DimPoint position = new DimPoint(currentX, ZERO);
      this.childrenRelBoxes.put(element, new DimRect(position, size));

      currentX = currentX.plus(elementMaxWidth.plus(this.elementPadding));
      containerHeight = Dim.max(containerHeight, size.getY());
    }

    return this.getFullBoxSize(new DimPoint(maxWidth, containerHeight));
  }
}
