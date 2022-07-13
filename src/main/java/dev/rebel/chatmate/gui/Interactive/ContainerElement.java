package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An element that contains other elements and is responsible for their relative layout. */
public abstract class ContainerElement extends ElementBase {
  private final LayoutMode mode;

  /** Subset of `this.children`. */
  protected final List<IElement> initialChildren;
  protected final List<IElement> children;
  protected final Map<IElement, DimRect> childrenRelBoxes;

  public ContainerElement(InteractiveContext context, IElement parent, LayoutMode mode) {
    super(context, parent);
    this.mode = mode;
    this.initialChildren = new ArrayList<>();
    this.children = new ArrayList<>();
    this.childrenRelBoxes = new HashMap<>();
  }

  @Override
  public void onInitialise() {
    this.clear();
    this.children.addAll(this.initialChildren);
  }

  protected ContainerElement addElement(IElement element) {
    if (element == null) {
      return this;
    }

    this.children.add(element);
    element.setParent(this);

    // If this is chained directly to the instantiation of the element, the child will be considered an "initial child"
    // and is NOT cleared when this container element is initialised.
    if (!super.isInitialised()) {
      this.initialChildren.add(element);
    } else {
      this.onInvalidateSize();
    }
    return this;
  }

  protected ContainerElement removeElement(IElement element) {
    this.children.remove(element);
    this.childrenRelBoxes.remove(element);
    this.onInvalidateSize();
    return this;
  }

  protected ContainerElement clear() {
    this.children.clear();
    this.childrenRelBoxes.clear();
    this.onInvalidateSize();
    return this;
  }

  protected List<IElement> getVisibleChildren() {
    return Collections.filter(this.children, IElement::getVisible);
  }

  @Override
  public List<IElement> getChildren() {
    return this.children;
  }

  @Override
  public DimPoint calculateThisSize(Dim maxWidth) {
    // note: the parent is responsible for the vertical/horizontal position of this container within itself.
    // we only need to set the relative positions of elements within the box that will be provided to us.
    List<Tuple2<IElement, DimPoint>> elementSizes = Collections.map(this.getVisibleChildren(), el -> new Tuple2<>(el, el.calculateSize(maxWidth)));
    if (elementSizes.size() == 0) {
      return new DimPoint(ZERO, ZERO);
    }

    if (this.mode == LayoutMode.BLOCK) {
      return this.calculateBlockSize(elementSizes, maxWidth);
    } else if (this.mode == LayoutMode.INLINE) {
      return this.calculateInlineSize(elementSizes, maxWidth);
    } else {
      throw new RuntimeException("Invalid layout mode " + this.mode);
    }
  }

  private DimPoint calculateBlockSize(List<Tuple2<IElement, DimPoint>> elementSizes, Dim maxWidth) {
    // calculate the size of each element, place them on separate lines.
    // at the end, position elements horizontally within our container width.
    Dim containerWidth;
    if (this.getSizingMode() == SizingMode.FILL) {
      containerWidth = maxWidth;
    } else {
      containerWidth = Collections.max(elementSizes, size -> size._2.getX().getGui())._2.getX();
    }

    Dim currentY = ZERO;
    for (Tuple2<IElement, DimPoint> elementSize : elementSizes) {
      IElement element = elementSize._1;
      DimPoint size = elementSize._2;

      Dim relX;
      if (element.getHorizontalAlignment() == HorizontalAlignment.LEFT) {
        relX = ZERO;
      } else if (element.getHorizontalAlignment() == HorizontalAlignment.CENTRE) {
        relX = containerWidth.minus(size.getX()).over(2);
      } else if (element.getHorizontalAlignment() == HorizontalAlignment.RIGHT) {
        relX = containerWidth.minus(size.getX());
      } else {
        throw new RuntimeException("Invalid horizontal layout " + element.getHorizontalAlignment());
      }

      this.childrenRelBoxes.put(element, new DimRect(relX, currentY, size.getX(), size.getY()));
      currentY = currentY.plus(size.getY());
    }

    return new DimPoint(containerWidth, currentY);
  }

  /** Given the children's box sizes, calculates this container size using the INLINE layout model. */
  protected final DimPoint calculateInlineSize(List<Tuple2<IElement, DimPoint>> elementSizes, Dim maxWidth) {
    // place one or more elements per line.
    // similar to the BLOCK calculation, except we also position elements vertically within their line.

    // first pass: group into lines
    List<List<Tuple2<IElement, DimPoint>>> lines = new ArrayList<>();
    List<Tuple2<IElement, DimPoint>> currentLine = new ArrayList<>();
    Dim lineX = ZERO;
    for (Tuple2<IElement, DimPoint> elementSize : elementSizes) {
      DimPoint size = elementSize._2;

      if (currentLine.size() > 0 && lineX.plus(size.getX()).gt(maxWidth)) {
        // doesn't fit on line
        lines.add(currentLine);
        currentLine = Collections.list(elementSize);
        lineX = size.getX();
      } else {
        // add to line
        currentLine.add(elementSize);
        lineX = lineX.plus(size.getX());
      }
    }
    lines.add(currentLine);

    // second pass: lay out elements within their lines. similar to the BLOCK layout algorithm
    Dim currentY = ZERO;
    Dim right = ZERO;
    for (List<Tuple2<IElement, DimPoint>> line : lines) {
      Dim lineHeight = Collections.max(line, size -> size._2.getY().getGui())._2.getY();
      Dim lineContentWidth = Collections.eliminate(Collections.map(line, l -> l._2.getX()), Dim::plus);
      Dim freeWidth = maxWidth.minus(lineContentWidth); // horizontal wiggle room

      // the line elements' horizontal alignment is prioritised from left to right.
      // so, if the first element is right-aligned, it will "squish over" all other element to the right.
      Dim currentX = ZERO;
      for (Tuple2<IElement, DimPoint> elementSize : line) {
        IElement element = elementSize._1;
        DimPoint size = elementSize._2;

        // calculate offset from currentX
        Dim xOffset;
        if (element.getHorizontalAlignment() == HorizontalAlignment.LEFT) {
          xOffset = ZERO;
        } else if (element.getHorizontalAlignment() == HorizontalAlignment.CENTRE) {
          xOffset = freeWidth.over(2);
        } else if (element.getHorizontalAlignment() == HorizontalAlignment.RIGHT) {
          xOffset = freeWidth;
        } else {
          throw new RuntimeException("Invalid horizontal layout " + element.getHorizontalAlignment());
        }
        freeWidth = freeWidth.minus(xOffset);

        // calculate offset form currentY
        Dim yOffset;
        if (element.getVerticalAlignment() == VerticalAlignment.TOP) {
          yOffset = ZERO;
        } else if (element.getVerticalAlignment() == VerticalAlignment.MIDDLE) {
          yOffset = lineHeight.minus(size.getY()).over(2);
        } else if (element.getVerticalAlignment() == VerticalAlignment.BOTTOM) {
          yOffset = lineHeight.minus(size.getY());
        } else {
          throw new RuntimeException("Invalid vertical layout " + element.getVerticalAlignment());
        }

        Dim thisRelX = currentX.plus(xOffset);
        Dim thisRelY = currentY.plus(yOffset);
        this.childrenRelBoxes.put(element, new DimRect(thisRelX, thisRelY, size.getX(), size.getY()));

        currentX = thisRelX.plus(size.getX());
        right = Dim.max(right, currentX);
      }

      currentY = currentY.plus(lineHeight);
    }

    Dim containerWidth;
    if (this.getSizingMode() == SizingMode.FILL) {
      containerWidth = maxWidth;
    } else {
      containerWidth = right;
    }

    return new DimPoint(containerWidth, currentY);
  }

  @Override
  public void renderElement() {
    for (IElement element : this.children) {
      if (element.getVisible()) {
        element.render();
      }
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

      element.setBox(relBox.withTranslation(contentBox.getPosition()));
    }
  }

  @Override
  public ContainerElement setVisible(boolean visible) {
    super.setVisible(visible);
    this.children.forEach(el -> el.setVisible(visible));
    return this;
  }

  /** Used for the automatic layout algorithm. */
  public enum LayoutMode {
    INLINE, // try to render multiple elements per line
    BLOCK // only one element per line
  }
}
