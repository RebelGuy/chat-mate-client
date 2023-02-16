package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ElementFactory;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout;
import dev.rebel.chatmate.gui.Interactive.Layout.LayoutGroup;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ChatMateHudStore {
  private final InteractiveContext hudContext; // it is essential that this is the same context instance as used for the HUD screen, otherwise elements won't render
  private final List<IHudStoreListener> listeners = new ArrayList<>();
  private final EmptyElement emptyElement = new EmptyElement();
  private boolean hudVisible = false;
  private List<HudElement> elements = new ArrayList<>();
  private Set<HudElement> selectedElements = new HashSet<>();

  public ChatMateHudStore(InteractiveContext hudContext) {
    this.hudContext = hudContext;
  }

  public void setElementSelection(HudElement element, boolean selected) {
    element.setSelected(selected);
    if (selected) {
      this.selectedElements.add(element);
    } else {
      this.selectedElements.remove(element);
    }
  }

  public void clearSelectedElements() {
    this.selectedElements.forEach(el -> el.setSelected(false));
    this.selectedElements.clear();
  }

  public List<HudElement> getSelectedElements() {
    return Collections.list(this.selectedElements);
  }

  /** Important: do NOT add elements before ChatMateHudScreen has been instantiated, otherwise you will get invalid parents. */
  public <TElement extends HudElement> TElement addElement(ElementFactory<TElement> createElement) {
    TElement element = createElement.create(this.hudContext, this.emptyElement);
    this.elements.add(element);
    this.listeners.forEach(listener -> listener.onAddElement(element));
    return element;
  }

  public void removeElement(HudElement element) {
    this.elements.remove(element);
    this.listeners.forEach(listener -> listener.onRemoveElement(element));
  }

  public List<HudElement> getElements() {
    return Collections.list(this.elements);
  }

  public boolean getVisible() {
    return this.hudVisible;
  }

  public void addListener(IHudStoreListener listener) {
    this.listeners.add(listener);
  }

  public interface IHudStoreListener {
    void onAddElement(HudElement element);
    void onRemoveElement(HudElement element);
    void onSetVisible(boolean visible);
  }

  // this is required because elements require a parent, but not all services that add Hud elements have access to
  // the HudScreen due to circular dependencies issues. as a hack, just provide this empty element as a parent. the
  // parent will be overwritten when the element gets added to the Hud container.
  private static class EmptyElement implements IElement {
    @Override
    public IElement getParent() {
      return null;
    }

    @Override
    public IElement setParent(IElement parent) {
      return null;
    }

    @Nullable
    @Override
    public List<IElement> getChildren() {
      return null;
    }

    @Override
    public void onInitialise() {

    }

    @Override
    public void onEvent(InteractiveEvent.EventType type, InteractiveEvent<?> event) {

    }

    @Override
    public void onCloseScreen() {

    }

    @Override
    public void onInvalidateSize() {

    }

    @Override
    public DimPoint calculateSize(Dim maxWidth) {
      return null;
    }

    @Override
    public DimPoint getLastCalculatedSize() {
      return null;
    }

    @Override
    public void setBox(DimRect box) {

    }

    @Override
    public DimRect getBox() {
      return null;
    }

    @Override
    public void render(@Nullable Consumer<Runnable> renderContextWrapper) {

    }

    @Override
    public boolean getVisible() {
      return false;
    }

    @Override
    public IElement setVisible(boolean visible) {
      return null;
    }

    @Override
    public Layout.RectExtension getPadding() {
      return null;
    }

    @Override
    public IElement setPadding(Layout.RectExtension padding) {
      return null;
    }

    @Override
    public Layout.RectExtension getBorder() {
      return null;
    }

    @Override
    public IElement setBorder(Layout.RectExtension border) {
      return null;
    }

    @Override
    public Layout.RectExtension getMargin() {
      return null;
    }

    @Override
    public IElement setMargin(Layout.RectExtension margin) {
      return null;
    }

    @Override
    public int getZIndex() {
      return 0;
    }

    @Override
    public int getEffectiveZIndex() {
      return 0;
    }

    @Override
    public IElement setZIndex(int zIndex) {
      return null;
    }

    @Override
    public int getDepth() {
      return 0;
    }

    @Override
    public @Nullable DimRect getVisibleBox() {
      return null;
    }

    @Override
    public IElement setVisibleBox(@Nullable DimRect visibleBox) {
      return null;
    }

    @Override
    public Layout.HorizontalAlignment getHorizontalAlignment() {
      return null;
    }

    @Override
    public IElement setHorizontalAlignment(Layout.HorizontalAlignment horizontalAlignment) {
      return null;
    }

    @Override
    public Layout.VerticalAlignment getVerticalAlignment() {
      return null;
    }

    @Override
    public IElement setVerticalAlignment(Layout.VerticalAlignment verticalAlignment) {
      return null;
    }

    @Override
    public Layout.SizingMode getSizingMode() {
      return null;
    }

    @Override
    public IElement setSizingMode(Layout.SizingMode sizingMode) {
      return null;
    }

    @Override
    public LayoutGroup getLayoutGroup() {
      return null;
    }

    @Override
    public IElement setLayoutGroup(LayoutGroup layoutGroup) {
      return null;
    }

    @Nullable
    @Override
    public String getTooltip() {
      return null;
    }

    @Override
    public IElement setTooltip(@Nullable String text) {
      return null;
    }

    @Override
    public IElement setOnClick(@Nullable Runnable onClick) {
      return null;
    }

    @Override
    public IElement setName(String name) {
      return null;
    }

    @Override
    public IElement setMaxWidth(@Nullable Dim maxWidth) {
      return null;
    }

    @Override
    public IElement setMaxContentWidth(@Nullable Dim maxContentWidth) {
      return null;
    }

    @Override
    public IElement setMinWidth(@Nullable Dim minWidth) {
      return this;
    }

    @Override
    public @Nullable Dim getMinWidth() {
      return null;
    }

    @Override
    public IElement setTargetHeight(@Nullable Dim height) {
      return null;
    }

    @Nullable
    @Override
    public Dim getTargetHeight() {
      return null;
    }

    @Override
    public @Nullable Dim getEffectiveTargetHeight() { return null; }

    @Override
    public IElement setTargetContentHeight(@Nullable Dim contentHeight) {
      return null;
    }

    @Nullable
    @Override
    public Dim getTargetContentHeight() {
      return null;
    }

    @Override
    public <T extends IElement> T cast() {
      return null;
    }
  }
}
