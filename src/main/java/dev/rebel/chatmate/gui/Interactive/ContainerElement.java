package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.LayoutGroup;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.EnumHelpers;
import scala.Tuple2;
import scala.Tuple3;

import javax.annotation.Nullable;
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

  public ContainerElement addElement(IElement element) {
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

  public ContainerElement removeElement(IElement element) {
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
  protected DimPoint calculateThisSize(Dim maxWidth) {
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
    List<Tuple2<IElement, DimPoint>> defaultLayoutGroupSizes = Collections.filter(elementSizes, x -> x._1.getLayoutGroup() == LayoutGroup.ALL);
    if (this.getSizingMode() == SizingMode.FILL) {
      containerWidth = maxWidth;
    } else {
      containerWidth = Collections.max(defaultLayoutGroupSizes, size -> size._2.getX().getGui())._2.getX();
    }

    Dim targetHeight = super.getTargetContentHeight();
    Dim contentHeight = Dim.sum(Collections.map(defaultLayoutGroupSizes, el -> el._2.getY()));
    Dim containerHeight;
    if (targetHeight == null) {
      containerHeight = contentHeight;
    } else {
      // use max so that we don't end up with negative numbers when positioning
      containerHeight = Dim.max(targetHeight, contentHeight);
    }

    List<Tuple2<List<Tuple2<IElement, DimPoint>>, VerticalAlignment>> verticalGroups = this.getVerticalGroups(elementSizes);
    List<Dim> elementOffsets = this.getVerticalOffsetsOfElements(verticalGroups, containerHeight);

    for (Tuple3<IElement, DimPoint, Dim> elementSize : Collections.map(elementSizes, (el, i) -> new Tuple3<>(el._1, el._2, elementOffsets.get(i)))) {
      IElement element = elementSize._1();
      DimPoint size = elementSize._2();
      Dim offset = elementSize._3();

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

      this.childrenRelBoxes.put(element, new DimRect(relX, offset, size.getX(), size.getY()));
    }

    return new DimPoint(containerWidth, containerHeight);
  }

  /** Gets the ordered list of vertically grouped elements. Each group is made up of successive elements of the provided list that have a matching vertical alignment. */
  private List<Tuple2<List<Tuple2<IElement, DimPoint>>, VerticalAlignment>> getVerticalGroups(List<Tuple2<IElement, DimPoint>> elementSizes) {
    List<Tuple2<List<Tuple2<IElement, DimPoint>>, VerticalAlignment>> groups = new ArrayList<>();

    @Nullable VerticalAlignment currentAlignment = null;
    List<Tuple2<IElement, DimPoint>> currentGroup = new ArrayList<>();
    for (Tuple2<IElement, DimPoint> elementSize : elementSizes) {
      VerticalAlignment thisAlignment = elementSize._1.getVerticalAlignment();
      boolean includedInLayout = elementSize._1.getLayoutGroup() == LayoutGroup.ALL;

      // new group
      if (currentAlignment != null && currentAlignment != thisAlignment && includedInLayout) {
        groups.add(new Tuple2<>(currentGroup, currentAlignment));
        currentGroup = new ArrayList<>();
      }

      // update current
      currentGroup.add(elementSize);
      currentAlignment = thisAlignment;
    }

    // flush
    if (currentGroup.size() > 0) {
      groups.add(new Tuple2<>(currentGroup, currentAlignment));
    }

    return groups;
  }

  /** Assuming each element is on its own line (BLOCK layout), returns the ordered y-offsets for the elements.
   * This works best with three (or less) groups, ordered as TOP -> MIDDLE -> BOTTOM. Adding more groups may result in undesired layouts. */
  private List<Dim> getVerticalOffsetsOfElements(List<Tuple2<List<Tuple2<IElement, DimPoint>>, VerticalAlignment>> groups, Dim height) {
    List<Dim> verticalOffsets = new ArrayList<>();

    // oof
    Dim remainingContentHeight = Dim.sum(
        Collections.map(
            groups,
            g -> Dim.sum(
                Collections.map(
                    Collections.filter(g._1, el -> el._1.getLayoutGroup() == LayoutGroup.ALL),
                    el -> el._2.getY()
                )
            )
        )
    );

    Dim currentY = ZERO;
    for (Tuple2<List<Tuple2<IElement, DimPoint>>, VerticalAlignment> group : groups) {
      // all elements in the group will be placed directly after one another, and the group itself will be placed according to its alignment
      VerticalAlignment groupAlignment = group._2;
      Dim groupHeight = Dim.sum(Collections.map(Collections.filter(group._1, el -> el._1.getLayoutGroup() == LayoutGroup.ALL), el -> el._2.getY()));

      if (groupAlignment == VerticalAlignment.TOP) {
        currentY = currentY;
      } else if (groupAlignment == VerticalAlignment.MIDDLE) {
        // align the group in the middle of the space
        currentY = currentY.plus(remainingContentHeight.minus(groupHeight).over(2));
      } else if (groupAlignment == VerticalAlignment.BOTTOM) {
        // every group hereafter will effectively also be aligned at the bottom
        currentY = height.minus(remainingContentHeight);
      } else {
        throw EnumHelpers.<VerticalAlignment>assertUnreachable(groupAlignment);
      }

      for (Tuple2<IElement, DimPoint> elementSize : group._1) {
        verticalOffsets.add(currentY);
        if (elementSize._1.getLayoutGroup() == LayoutGroup.ALL) {
          currentY = currentY.plus(elementSize._2.getY());
        }
      }

      remainingContentHeight = remainingContentHeight.minus(groupHeight);
    }

    return verticalOffsets;
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
      boolean participatesInLayout = elementSize._1.getLayoutGroup() == LayoutGroup.ALL;
      DimPoint size = elementSize._2;

      if (currentLine.size() > 0 && lineX.plus(size.getX()).gt(maxWidth)) {
        // doesn't fit on line
        lines.add(currentLine);
        currentLine = Collections.list(elementSize);
        lineX = participatesInLayout ? size.getX() : ZERO;
      } else {
        // add to line
        currentLine.add(elementSize);
        lineX = participatesInLayout ? lineX.plus(size.getX()) : lineX;
      }
    }
    lines.add(currentLine);

    // second pass: lay out elements within their lines. similar to the BLOCK layout algorithm
    Dim currentY = ZERO;
    Dim right = ZERO;
    for (List<Tuple2<IElement, DimPoint>> line : lines) {
      List<Tuple2<IElement, DimPoint>> participatingLines = Collections.filter(line, l -> l._1.getLayoutGroup() == LayoutGroup.ALL);
      Dim lineHeight = Dim.max(Collections.map(participatingLines, l -> l._2.getY()));
      Dim lineContentWidth = Dim.sum(Collections.map(participatingLines, l -> l._2.getX()));
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
        boolean isParticipating = element.getLayoutGroup() == LayoutGroup.ALL;
        DimPoint effectiveSize = isParticipating ? size : new DimPoint(ZERO, ZERO);
        this.childrenRelBoxes.put(element, new DimRect(thisRelX, thisRelY, effectiveSize.getX(), effectiveSize.getY()));

        if (isParticipating) {
          currentX = thisRelX.plus(size.getX());
          right = Dim.max(right, currentX);
        }
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
  protected void renderElement() {
    for (IElement element : this.children) {
      if (element.getVisible()) {
        element.render(null);
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
    INLINE, // try to render multiple elements per line. vertical positioning of elements is done according to the total height of the row of elements they are part of
    BLOCK // only one element per line. vertical positioning of elements is done according to the `targetHeight`, if set. Note that `FILL` layout mode does not affect the vertical size.
  }
}
