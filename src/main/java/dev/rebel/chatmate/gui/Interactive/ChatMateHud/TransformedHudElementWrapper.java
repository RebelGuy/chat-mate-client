package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

/** Converts an Element into a HudElement, automatically scaling and translating the rendering.
 * This should be used if the underlying element does not know about the transformation. Since
 * we are deferring the rendering of children, the transformation won't be applied to the children,
 * which is why `TElement` should be a `SingleElement`. */
public class TransformedHudElementWrapper<TElement extends SingleElement> extends HudElement {
  public TElement element;

  public TransformedHudElementWrapper(InteractiveContext context, IElement parent) {
    super(context, parent);
  }

  public TElement setElement(BiFunction<InteractiveContext, IElement, TElement> createElement) {
    TElement element = createElement.apply(super.context, this);
    return this.setElement(element);
  }

  public TElement setElement(TElement element) {
    if (this.element != element) {
      this.element = element;
      super.onInvalidateSize();
    }

    return this.element;
  }

  @Override
  public @Nullable List<IElement> onGetChildren() {
    return Collections.list(this.element);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    if (this.element == null) {
      return new DimPoint(ZERO, ZERO);
    }

    Dim scaledMaxContentSize = maxContentSize.times(1 / this.currentScale);
    DimPoint scaledSize = this.element.calculateSize(scaledMaxContentSize);
    return scaledSize.scale(this.currentScale);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    if (this.element == null) {
      return;
    }

    // the underlying element's box is implicitly anchored at the top left, so we can safely scale the size like this.
    // todo: this causes the child element box to look weird in the debug menu, because it's not scaled. not sure what the correct solution is
    DimPoint newPosition = new DimPoint(ZERO, ZERO);
    DimPoint newSize = box.getSize().scale(1 / this.currentScale);
    this.element.setBox(box.withPosition(newPosition).withSize(newSize));
  }

  @Override
  public void onRenderElement() {
    if (this.element == null) {
      return;
    }

    // implicitly anchored at the top left
    this.element.render(r -> RendererHelpers.withMapping(super.getBox().getPosition(), this.currentScale, r));
  }
}
