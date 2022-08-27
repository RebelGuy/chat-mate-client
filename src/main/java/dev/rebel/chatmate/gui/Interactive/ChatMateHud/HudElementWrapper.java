package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

/** Converts an Element into a HudElement. */
public class HudElementWrapper<TElement extends IElement> extends HudElement {
  public TElement element;

  public HudElementWrapper(InteractiveContext context, IElement parent) {
    super(context, parent);
  }

  public TElement setElement(BiFunction<InteractiveContext, IElement, TElement> createElement) {
    TElement element = createElement.apply(super.context, this);

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

    Dim scaledMaxContentSize = maxContentSize.times(1 / this.currentScale);
    DimPoint scaledSize = this.element.calculateSize(scaledMaxContentSize);
    return scaledSize.scale(this.currentScale);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    if (this.element == null) {
      return;
    }

    // the label box is implicitly anchored at the top left, so we can safely scale the size like this
    this.element.setBox(box.withSize(box.getSize().scale(1 / this.currentScale)));
  }

  @Override
  protected void onRescaleContent(DimRect oldBox, float oldScale, float newScale) {
    this.anchor = calculateAnchor(super.context.dimFactory.getMinecraftRect(), oldBox);
  }

  @Override
  protected void renderElement() {
    if (this.element == null) {
      return;
    }

    RendererHelpers.withMapping(new DimPoint(ZERO, ZERO), this.currentScale, () -> {
      this.element.render();
    });
  }
}
