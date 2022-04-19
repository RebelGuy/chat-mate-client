package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;

/** A single element which does not automatically manage its childrens' layout. Intended for standalone elements. */
public abstract class SingleElement extends ElementBase {
  public SingleElement(InteractiveContext context, IElement parent) {
    super(context, parent);
  }

  @Override
  public abstract DimPoint calculateThisSize(Dim maxContentSize);

  @Override
  public abstract void renderElement();
}
