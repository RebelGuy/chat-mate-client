package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An element that contains other elements and is responsible for their relative layout. */
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

  protected ContainerElement addElement(IElement element) {
    this.children.add(element);
    element.setParent(this);
    return this;
  }

  protected ContainerElement removeElement(IElement element) {
    this.children.remove(element);
    this.childrenRelBoxes.remove(element);
    return this;
  }

  protected ContainerElement clear() {
    this.children.clear();
    this.childrenRelBoxes.clear();
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return this.children;
  }

  @Override
  public DimPoint calculateSize(Dim maxWidth) {
    maxWidth = this.getContentBoxWidth(maxWidth);

    Dim containerWidth = ZERO;
    Dim containerHeight = ZERO;

    Dim currentX = ZERO;
    Dim currentY = ZERO;
    List<Dim> heightsInCurrentLine = new ArrayList<>();
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
          heightsInCurrentLine.add(size.getY());

        } else {
          // new line
          if (heightsInCurrentLine.size() > 0) {
            currentY = currentY.plus(Dim.max(heightsInCurrentLine));
            heightsInCurrentLine.clear();
          }

          currentX = ZERO;
          currentY = currentY.plus(size.getY());
          DimPoint position = new DimPoint(currentX, currentY);
          this.childrenRelBoxes.put(element, new DimRect(position, size));
          heightsInCurrentLine.add(size.getY());
        }

        currentX = currentX.plus(size.getX());
        containerWidth = Dim.max(containerWidth, currentX);

      } else {
        throw new RuntimeException("Invalid layout mode " + this.mode);
      }
    }

    if (heightsInCurrentLine.size() > 0) {
      currentY = currentY.plus(Dim.max(heightsInCurrentLine));
      containerHeight = Dim.max(containerHeight, currentY);
      heightsInCurrentLine.clear();
    }

    return this.getFullBoxSize(new DimPoint(containerWidth, containerHeight));
  }

  @Override
  public void renderElement() {
    for (IElement element : this.children) {
      element.render();
    }
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    DimRect contentBox = this.getContentBox();
    for (IElement element : this.children) {
      DimRect relBox = this.childrenRelBoxes.get(element);
      if (relBox == null) {
        continue;
      }
      // todo: add horizontal alignment mode that allows us to align items left, centre, or right
      // and vertical alignment as well (for elements on the same line)
      element.setBox(relBox.withTranslation(contentBox.getPosition()));
    }
  }

  @Override
  public ContainerElement setVisible(boolean visible) {
    this.children.forEach(el -> el.setVisible(visible));
    return this;
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
