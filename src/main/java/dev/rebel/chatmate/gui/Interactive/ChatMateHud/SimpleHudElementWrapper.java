package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

/** Converts an Element into a HudElement, without scaling or translating the element.
 * The local space will already contain the transformation, and the element is expected
 * to handle any scaling by itself. */
public class SimpleHudElementWrapper<TElement extends IElement> extends HudElement {
  public TElement element;

  public SimpleHudElementWrapper(InteractiveContext context, IElement parent) {
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
  public @Nullable List<IElement> getChildren() {
    return Collections.list(this.element);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    if (this.element == null) {
      return new DimPoint(ZERO, ZERO);
    }

    return this.element.calculateSize(maxContentSize);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    if (this.element == null) {
      return;
    }

    this.element.setBox(box);
  }

  @Override
  public void onRenderElement() {
    if (this.element == null) {
      return;
    }

    this.element.render(null);
  }
}
