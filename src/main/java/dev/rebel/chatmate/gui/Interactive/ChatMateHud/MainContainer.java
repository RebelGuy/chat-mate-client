package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudFilters.IHudFilter;
import dev.rebel.chatmate.gui.Interactive.DropElement.IDropElementListener;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainContainer extends ElementBase implements IDropElementListener {
  private final ChatMateHudStore store;

  // we have to keep our own list of HudElements (rather than just relying on the store) because additions/removals are run as side effects
  private final List<HudElement> children;

  private @Nullable DropElement dropElement;
  private @Nullable Runnable onToggleSelection; // the optional callback for toggling the selection to be called when the mouse button is lifted
  private boolean isMultiSelect; // whether we are currently in multi-select mode

  public MainContainer(ChatMateHudStore store, InteractiveScreen.InteractiveContext context, IElement parent) {
    super(context, parent);
    this.store = store;
    this.children = store.getElements();
    this.dropElement = null;
    this.onToggleSelection = null;
    this.isMultiSelect = false;
  }

  private boolean shouldRenderElement(HudElement element) {
    if (!super.context.config.getChatMateEnabledEmitter().get() || !super.context.config.getHudEnabledEmitter().get()) {
      return false;
    }

    if (!ElementHelpers.isCompletelyVisible(element)) {
      return false;
    }

    List<IHudFilter> filters = element.getHudElementFilter();
    if (filters == null) {
      return true;
    }

    GuiScreen currentScreen = super.context.minecraft.currentScreen;
    boolean isWhitelisted = Collections.any(filters, f -> Objects.equals(f.isWhitelisted(currentScreen), true));
    boolean isBlacklisted = Collections.any(filters, f -> Objects.equals(f.isBlacklisted(currentScreen), true));

    return isWhitelisted && !isBlacklisted;
  }

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

  private boolean isHoldingShift() {
    return super.context.keyboardEventService.isHeldDown(Keyboard.KEY_LSHIFT);
  }

  @Override
  public void onMouseDown(IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      DimPoint position = e.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);

      @Nullable HudElement selectedElement = null;
      for (HudElement element : this.store.getElements()) {
        if (!this.shouldRenderElement(element) || !element.canDrag() && !element.canScale()) {
          continue;
        }

        if (getCollisionBox(element).checkCollision(position)) {
          selectedElement = element;
          break;
        }
      }

      // i.e. clickaway clears selection
      if (selectedElement == null) {
        if (!this.isHoldingShift()) {
          this.store.clearSelectedElements();
          this.dropElement = null;
          this.isMultiSelect = false;
          super.onInvalidateSize();
        }
        return;
      }

      // keep multi select mode when dragging an already selected element, even once shift is no longer held down. otherwise, disable multi select mode
      boolean currentlySelected = this.store.getSelectedElements().contains(selectedElement);
      this.isMultiSelect = this.isHoldingShift() || this.isMultiSelect && currentlySelected;
      if (!this.isMultiSelect) {
        this.store.clearSelectedElements();
      }

      // enable toggle selection. we only know whether we want to toggle in the onDrop callback, but at that point we don't know what the element and selected state should be.
      // that's why the toggling function is defined here, but executed later
      if (this.isHoldingShift()) {
        HudElement _selectedElement = selectedElement;
        this.onToggleSelection = () -> this.store.setElementSelection(_selectedElement, !currentlySelected);
      }

      this.dropElement = new DropElement(super.context, this, this);
      this.store.setElementSelection(selectedElement, true);
      super.onInvalidateSize();
    }
  }

  @Override
  public void onDrag(DimPoint prevPosition, DimPoint currentPosition) {
    DimPoint positionDelta = currentPosition.minus(prevPosition).setAnchor(DimAnchor.GUI);
    for (HudElement element : this.store.getSelectedElements()) {
      element.onDrag(positionDelta);
    }
  }

  @Override
  public void onDrop(DimPoint startPosition, DimPoint currentPosition, Dim totalDistanceTravelled) {
    if (!this.isMultiSelect) {
      // if dragging a single element by itself, unselect it now
      this.store.clearSelectedElements();
    } else if (this.onToggleSelection != null && totalDistanceTravelled.equals(ZERO)) {
      this.onToggleSelection.run();
    }
    this.onToggleSelection = null;
    this.dropElement = null;
    super.onInvalidateSize();
  }

  @Override
  public void onMouseScroll(IEvent<MouseEventData.In> e) {
    DimPoint position = e.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);
    @Nullable HudElement hoveredElement = null;
    for (HudElement element : this.store.getElements()) {
      if (!this.shouldRenderElement(element) || !element.canScale()) {
        continue;
      }

      if (getCollisionBox(element).checkCollision(position)) {
        hoveredElement = element;
        break;
      }
    }

    if (hoveredElement == null) {
      return;
    }

    Set<HudElement> elements = new HashSet<>(this.store.getSelectedElements());
    elements.add(hoveredElement);
    elements.forEach(el -> el.onScroll(e.getData().mouseScrollData.scrollDirection));

    e.stopPropagation();
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxWidth) {
    // all children of this element are expected to be floating elements with arbitrary positions
    // regardless of contents, this is always a "fullscreen" element
    this.getChildren().forEach(element -> element.calculateSize(maxWidth));
    return super.context.dimFactory.getMinecraftSize();
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    // lol
    List<IElement> children = Collections.map(this.children, el -> el);
    if (this.dropElement != null) {
      children.add(this.dropElement);
    }
    return children;
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    // pass through the full box and let the children handle setting their box themselves
    DimRect contentBox = this.getContentBox();
    for (IElement element : this.getChildren()) {
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
      if (this.shouldRenderElement(element)) {
        element.render(null);
      }
    }
  }
}
