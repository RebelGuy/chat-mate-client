package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** An element that contains other element and is responsible for their relative layout. */
public abstract class ContainerElement extends ElementBase {
  private final LayoutMode mode;

  protected final List<IElement> children;
  protected final Map<IElement, DimRect> childrenRelBoxes;

  public ContainerElement(InteractiveContext context, IElement parent, LayoutMode mode) {
    super(context, parent);
    this.mode = mode;
    this.children = new ArrayList<>();
    this.childrenRelBoxes = new HashMap<>();
  }

  public void addElement(IElement element) {
    this.children.add(element);
    this.onInvalidateSize();
  }

  public void removeElement(IElement element) {
    this.children.remove(element);
    this.childrenRelBoxes.remove(element);
    this.onInvalidateSize();
  }

  public void clear() {
    this.children.clear();
    this.childrenRelBoxes.clear();
    this.onInvalidateSize();
  }

  @Override
  public boolean onMouseDown(MouseEventData.In in) {
    for (IElement element : this.children) {
      if (propagateMouseEvent(in, element, element::onMouseDown)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean onMouseMove(MouseEventData.In in) {
    for (IElement element : this.children) {
      if (propagateMouseEvent(in, element, element::onMouseMove)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean onMouseUp(MouseEventData.In in) {
    for (IElement element : this.children) {
      if (propagateMouseEvent(in, element, element::onMouseUp)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean onMouseScroll(MouseEventData.In in) {
    for (IElement element : this.children) {
      if (propagateMouseEvent(in, element, element::onMouseScroll)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean onKeyDown(KeyboardEventData.In in) {
    for (IElement element : this.children) {
      if (element.onKeyDown(in)) {
        return true;
      }
    }

    return false;
  }

  /** Dispatches the event to the element, if eligible. */
  protected boolean propagateMouseEvent(MouseEventData.In in, IElement element, Function<MouseEventData.In, Boolean> dispatcher) {
    if (!element.getVisible()) {
      return false;
    }

    DimRect rect = element.getBox();
    if (rect == null) {
      return false;
    }

    Dim x = in.mousePositionData.x;
    Dim y = in.mousePositionData.y;
    if (rect.checkCollision(new DimPoint(x, y))) {
      return dispatcher.apply(in);
    }

    return false;
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    Dim containerWidth = this.context.dimFactory.zeroGui();
    Dim containerHeight = this.context.dimFactory.zeroGui();

    Dim currentX = this.context.dimFactory.zeroGui();
    Dim currentY = this.context.dimFactory.zeroGui();
    for (IElement element : this.children) {
      DimPoint size = element.calculateSize(maxWidth);

      if (this.mode == LayoutMode.BLOCK) {
        // one per line
        DimPoint position = new DimPoint(currentX, currentY);
        this.childrenRelBoxes.put(element, new DimRect(position, size));

        containerWidth = Dim.max(containerWidth, size.getX());
        containerHeight = containerHeight.plus(size.getY());
        currentY = containerHeight;

      } else if (this.mode == LayoutMode.INLINE) {
        // try to fit multiple elements per line
        if (currentX.plus(size.getX()).lte(maxWidth)) {
          // add to line
          DimPoint position = new DimPoint(currentX, currentY);
          this.childrenRelBoxes.put(element, new DimRect(position, size));

        } else {
          // new line
          currentX = this.context.dimFactory.zeroGui();
          currentY = currentY.plus(size.getY());
          DimPoint position = new DimPoint(currentX, currentY);
          this.childrenRelBoxes.put(element, new DimRect(position, size));
        }

        currentX = currentX.plus(size.getX());
        containerWidth = Dim.max(containerWidth, currentX);
        containerHeight = Dim.max(containerHeight, currentY);

      } else {
        throw new RuntimeException("Invalid layout mode " + this.mode);
      }
    }

    return new DimPoint(containerWidth, containerHeight);
  }

  @Override
  public final void render() {
    for (IElement element : this.children) {
      element.render();
    }
  }

  @Override
  public void setBox(DimRect box) {
    this.box = box;
    for (IElement element : this.children) {
      DimRect relBox = this.childrenRelBoxes.get(element);
      if (relBox == null) {
        continue;
      }
      element.setBox(relBox.withTranslation(box.getPosition()));
    }
  }

  @Override
  public void setVisible(boolean visible) {
    this.children.forEach(el -> el.setVisible(visible));
  }

  @Override
  public boolean getVisible() {
    return this.children.stream().anyMatch(IElement::getVisible);
  }

  /** Used for the automatic layout algorithm. */
  public enum LayoutMode {
    INLINE, // try to render multiple elements per line
    BLOCK // only one element per line
  }
}
