package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore.IHudStoreListener;
import dev.rebel.chatmate.gui.Interactive.ContainerElement;
import dev.rebel.chatmate.gui.Interactive.ElementBase;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MainContainer extends ElementBase {

  // we have to keep our own list of HudElements (rather than just relying on the store) because additions/removals are run as side effects
  private final List<HudElement> children;

  public MainContainer(ChatMateHudStore store, InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);

    this.children = store.getElements();
  }

  // todo: the screen should be the listener, not this element. the screen then calls "MainContainer::addElement", etc
  public void addElement(HudElement hudElement) {
    if (hudElement == null) {
      return;
    }

    super.context.renderer.runSideEffect(() -> {
      this.children.add(hudElement);
      hudElement.setParent(this);
      super.onInvalidateSize();
    });
  }

  public void removeElement(HudElement hudElement) {
    if (hudElement == null) {
      return;
    }

    super.context.renderer.runSideEffect(() -> {
      this.children.remove(hudElement);
      super.onInvalidateSize();
    });
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxWidth) {
    // all children of this element are expected to be floating elements with arbitrary positions
    // regardless of contents, this is always a "fullscreen" element
    this.children.forEach(element -> element.calculateSize(maxWidth));
    return super.context.dimFactory.getMinecraftSize();
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    // lol
    return Collections.map(this.children, el -> el);
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    // pass through the full box and let the children handle setting their box themselves
    DimRect contentBox = this.getContentBox();
    for (HudElement element : this.children) {
      element.setBox(contentBox);
    }
  }

  @Override
  public MainContainer setVisible(boolean visible) {
    super.setVisible(visible);
    this.children.forEach(el -> el.setVisible(visible));
    return this;
  }

  @Override
  protected void renderElement() {
    for (HudElement element : this.children) {
      if (element.getVisible()) {
        element.render(null);
      }
    }
  }
}
