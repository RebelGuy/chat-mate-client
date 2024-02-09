package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.LayoutGroup;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.EnumHelpers;
import scala.Tuple2;
import scala.Tuple3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static dev.rebel.chatmate.util.Objects.firstOrNull;

/** An element that contains other elements and is responsible for their relative layout. */
public abstract class ContainerElement extends ElementBase {
  private final LayoutMode mode;

  /** Subset of `this.children`. */
  protected final List<IElement> initialChildren;
  protected final List<IElement> children;
  protected final Map<IElement, DimRect> childrenRelBoxes;

  private boolean allowShrink = false;

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

  public ContainerElement addElement(@Nullable IElement element) {
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

  /** For INLINE elements, whether children should be shrunk (if a min size is set) to optimise the per-line layout.
   * If false, elements are given the maximum width and wrapped to the next line if required.
   * If true, elements with a set min size will be sized dynamically to fit the maximum number of elements onto a line while avoiding a gap. */
  public ContainerElement setAllowShrink(boolean allowShrink) {
    if (this.allowShrink != allowShrink) {
      this.allowShrink = allowShrink;
      super.onInvalidateSize();
    }
    return this;
  }

  @Override
  public List<IElement> getChildren() {
    return this.children;
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxWidth) {
    if (this.getVisibleChildren().size() == 0) {
      return new DimPoint(ZERO, ZERO);
    }

    // note: the parent is responsible for the vertical/horizontal position of this container within itself.
    // we only need to set the relative positions of elements within the box that will be provided to us.
    if (this.mode == LayoutMode.BLOCK) {
      List<Tuple2<IElement, DimPoint>> elementSizes = Collections.map(this.getVisibleChildren(), el -> new Tuple2<>(el, el.calculateSize(maxWidth)));
      return this.calculateBlockSize(elementSizes, maxWidth);
    } else if (this.mode == LayoutMode.INLINE) {
      return this.calculateInlineSize(maxWidth, null);
    } else {
      throw EnumHelpers.<LayoutMode>assertUnreachable(this.mode);
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

    List<Tuple2<List<Tuple2<IElement, DimPoint>>, VerticalAlignment>> verticalGroups = this.getAlignmentGroups(elementSizes, IElement::getVerticalAlignment);
    OffsetResult offsetResult = this.getElementOffsets(verticalGroups, containerHeight, DimPoint::getY, algn -> algn == VerticalAlignment.TOP ? 1 : algn == VerticalAlignment.MIDDLE ? 2 : 3);
    List<Dim> elementOffsets = offsetResult.offsets;

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
        throw EnumHelpers.<HorizontalAlignment>assertUnreachable(element.getHorizontalAlignment());
      }

      this.childrenRelBoxes.put(element, new DimRect(relX, offset, size.getX(), size.getY()));
    }

    // we always keep the height the same, so don't need to worry about `offsetResult.keepContainerSize`.
    return new DimPoint(containerWidth, containerHeight);
  }

  /** Gets the ordered list of alignment-grouped elements. Each group is made up of successive elements of the provided list that have a matching alignment. */
  private <TAlignment> List<Tuple2<List<Tuple2<IElement, DimPoint>>, TAlignment>> getAlignmentGroups(List<Tuple2<IElement, DimPoint>> elementSizes, Function<IElement, TAlignment> getAlignment) {
    List<Tuple2<List<Tuple2<IElement, DimPoint>>, TAlignment>> groups = new ArrayList<>();

    @Nullable TAlignment currentAlignment = null;
    List<Tuple2<IElement, DimPoint>> currentGroup = new ArrayList<>();
    for (Tuple2<IElement, DimPoint> elementSize : elementSizes) {
      TAlignment thisAlignment = getAlignment.apply(elementSize._1);
      boolean includedInLayout = elementSize._1.getLayoutGroup() == LayoutGroup.ALL;

      // new group
      if (currentAlignment != null && currentAlignment != thisAlignment && includedInLayout) {
        groups.add(new Tuple2<>(currentGroup, currentAlignment));
        currentGroup = new ArrayList<>();
      }

      // update current
      currentGroup.add(elementSize);
      if (includedInLayout) {
        currentAlignment = thisAlignment;
      }
    }

    // flush
    if (currentGroup.size() > 0) {
      groups.add(new Tuple2<>(currentGroup, currentAlignment));
    }

    return groups;
  }

  /** Returns the ordered offsets for the elements such that each group is aligned correctly in the available space..
   * This works best with three (or less) groups, ordered as NEAR SIDE -> CENTRE -> FAR SIDE. Adding more groups may result in undesired layouts.
   *
   * `getAlignmentOrder` should return either 1, 2, or 3. */
  private <TAlignment> OffsetResult getElementOffsets(List<Tuple2<List<Tuple2<IElement, DimPoint>>, TAlignment>> groups, Dim containerSize, Function<DimPoint, Dim> getSize, Function<TAlignment, Integer> getAlignmentOrder) {
    List<Dim> elementOffsets = new ArrayList<>();

    // oof
    Dim remainingContentSize = Dim.sum(
        Collections.map(
            groups,
            g -> Dim.sum(
                Collections.map(
                    Collections.filter(g._1, el -> el._1.getLayoutGroup() == LayoutGroup.ALL),
                    el -> getSize.apply(el._2)
                )
            )
        )
    );

    Dim currentPos = ZERO;
    boolean requiresContainerSize = false;
    for (Tuple2<List<Tuple2<IElement, DimPoint>>, TAlignment> group : groups) {
      // all elements in the group will be placed directly after one another, and the group itself will be placed according to its alignment
      int alignmentOrder = getAlignmentOrder.apply(group._2);
      Dim groupSize = Dim.sum(Collections.map(Collections.filter(group._1, el -> el._1.getLayoutGroup() == LayoutGroup.ALL), el -> getSize.apply(el._2)));

      if (alignmentOrder == 1) {
        currentPos = currentPos;
      } else if (alignmentOrder == 2) {
        // align the group in the middle of the space
        currentPos = currentPos.plus(remainingContentSize.minus(groupSize).over(2));

        // if the container size were to change, then the calculated position may no longer be centred. therefore, the final container size should be the same size passed into this method.
        requiresContainerSize = true;
      } else if (alignmentOrder == 3) {
        // every group hereafter will effectively also be aligned at the far side
        currentPos = containerSize.minus(remainingContentSize);
      } else {
        throw new RuntimeException("Invalid alignment order " + alignmentOrder);
      }

      for (Tuple2<IElement, DimPoint> elementSize : group._1) {
        elementOffsets.add(currentPos);
        if (elementSize._1.getLayoutGroup() == LayoutGroup.ALL) {
          currentPos = currentPos.plus(getSize.apply(elementSize._2));
        }
      }

      remainingContentSize = remainingContentSize.minus(groupSize);
    }

    return new OffsetResult(elementOffsets, requiresContainerSize);
  }

  /** Calculates this container size using the INLINE layout model. Uses the sizes provided, or calculates them using the container's settings. */
  protected final DimPoint calculateInlineSize(Dim maxWidth, @Nullable List<Tuple2<IElement, DimPoint>> elementSizes) {
    // place one or more elements per line.
    // similar to the BLOCK calculation, except we also position elements vertically within their line.

    // first pass: group into lines
    List<List<Tuple2<IElement, DimPoint>>> lines;
    if (this.allowShrink && elementSizes == null) { // don't shrink if element sizes are provided
      lines = calculateDynamicLinesForInline(maxWidth);
    } else {
      if (elementSizes == null) {
        elementSizes = Collections.map(this.getVisibleChildren(), el -> new Tuple2<>(el, el.calculateSize(maxWidth)));
      }
      lines = calculateStaticLinesForInline(maxWidth, elementSizes);
    }

    // second pass: lay out elements within their lines. similar to the BLOCK layout algorithm
    Dim currentY = ZERO;
    Dim right = ZERO;
    boolean requiresMaxWidth = false;
    for (List<Tuple2<IElement, DimPoint>> line : lines) {
      List<Tuple2<IElement, DimPoint>> participatingItems = Collections.filter(line, l -> l._1.getLayoutGroup() == LayoutGroup.ALL);

      Dim lineHeight = ZERO;
      if (participatingItems.size() > 0) {
        lineHeight = Dim.max(Collections.map(participatingItems, l -> l._2.getY()));
      }

      List<Tuple2<List<Tuple2<IElement, DimPoint>>, HorizontalAlignment>> horizontalGroups = this.getAlignmentGroups(line, IElement::getHorizontalAlignment);
      OffsetResult offsetResult = this.getElementOffsets(horizontalGroups, maxWidth, DimPoint::getX, algn -> algn == HorizontalAlignment.LEFT ? 1 : algn == HorizontalAlignment.CENTRE ? 2 : 3);
      List<Dim> elementOffsets = offsetResult.offsets;
      if (offsetResult.requiresContainerSize) {
        requiresMaxWidth = true;
      }

      // the line elements' horizontal alignment is prioritised from left to right.
      // so, if the first element is right-aligned, it will "squish over" all other element to the right.
      for (Tuple3<IElement, DimPoint, Dim> elementSize : Collections.map(line, (el, i) -> new Tuple3<>(el._1, el._2, elementOffsets.get(i)))) {
        IElement element = elementSize._1();
        DimPoint size = elementSize._2();
        Dim xOffset = elementSize._3();

        // calculate offset from currentY
        Dim yOffsetRel;
        if (element.getVerticalAlignment() == VerticalAlignment.TOP) {
          yOffsetRel = ZERO;
        } else if (element.getVerticalAlignment() == VerticalAlignment.MIDDLE) {
          yOffsetRel = lineHeight.minus(size.getY()).over(2);
        } else if (element.getVerticalAlignment() == VerticalAlignment.BOTTOM) {
          yOffsetRel = lineHeight.minus(size.getY());
        } else {
          throw EnumHelpers.<VerticalAlignment>assertUnreachable(element.getVerticalAlignment());
        }

        boolean isParticipating = element.getLayoutGroup() == LayoutGroup.ALL;
        this.childrenRelBoxes.put(element, new DimRect(xOffset, currentY.plus(yOffsetRel), size.getX(), size.getY()));

        if (isParticipating) {
          right = Dim.max(right, xOffset.plus(size.getX()));
        }
      }

      currentY = currentY.plus(lineHeight);
    }

    Dim containerWidth;
    if (this.getSizingMode() == SizingMode.FILL || requiresMaxWidth) {
      containerWidth = maxWidth;
    } else {
      containerWidth = right;
    }

    return new DimPoint(containerWidth, currentY);
  }

  /** Uses the provided sizes for each element, and wraps them to the next line if they don't fit on the current one. */
  private List<List<Tuple2<IElement, DimPoint>>> calculateStaticLinesForInline(Dim maxWidth, List<Tuple2<IElement, DimPoint>> elementSizes) {
    List<List<Tuple2<IElement, DimPoint>>> lines = new ArrayList<>();
    List<Tuple2<IElement, DimPoint>> currentLine = new ArrayList<>();
    Dim lineX = ZERO;
    for (Tuple2<IElement, DimPoint> elementSize : elementSizes) {
      IElement element = elementSize._1;
      DimPoint size = elementSize._2;
      boolean participatesInLayout = element.getLayoutGroup() == LayoutGroup.ALL;

      // non-participating elements may clip the right side - that's what they signed up for
      if (currentLine.size() > 0 && participatesInLayout && lineX.plus(size.getX()).gt(maxWidth)) {
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

    return lines;
  }

  /** For elements that have a minWidth set, will shrink those elements (up to the minWidth) to fill lines and fit as many elements on each line as possible. */
  private List<List<Tuple2<IElement, DimPoint>>> calculateDynamicLinesForInline(Dim maxWidth) {
    List<List<Tuple2<IElement, DimPoint>>> lines = new ArrayList<>();
    List<Tuple2<IElement, DimPoint>> currentLine = new ArrayList<>();
    Dim lineX = ZERO;
    for (IElement element : this.getVisibleChildren()) {
      boolean participatesInLayout = element.getLayoutGroup() == LayoutGroup.ALL;
      DimPoint size = element.calculateSize(firstOrNull(element.getMinWidth(), maxWidth)); // use the minimum size
      Tuple2<IElement, DimPoint> elementSize = new Tuple2<>(element, size);

      // non-participating elements may clip the right side - that's what they signed up for
      if (currentLine.size() > 0 && participatesInLayout && lineX.plus(size.getX()).gt(maxWidth)) {
        // doesn't fit on line
        lines.add(this.expandItemsInLine(currentLine, maxWidth));
        currentLine = Collections.list(elementSize);
        lineX = participatesInLayout ? size.getX() : ZERO;
      } else {
        // add to line
        currentLine.add(elementSize);
        lineX = participatesInLayout ? lineX.plus(size.getX()) : lineX;
      }
    }
    lines.add(this.expandItemsInLine(currentLine, maxWidth));

    return lines;
  }

  /** Tries to proportionally expand elements which have the minWidth set until they fill the line. */
  private List<Tuple2<IElement, DimPoint>> expandItemsInLine(List<Tuple2<IElement, DimPoint>> minimisedLine, Dim desiredWidth) {
    Dim remainingSpace = desiredWidth.minus(Dim.sum(Collections.map(minimisedLine, line -> line._2.getX())));
    if (remainingSpace.lte(ZERO)) {
      return minimisedLine;
    }

    List<Tuple2<IElement, DimPoint>> expandableElementSizes = Collections.filter(minimisedLine, el -> el._1.getMinWidth() != null);
    Map<IElement, Dim> maxWidths = new HashMap<>();
    for (Tuple2<IElement, DimPoint> elementSize : expandableElementSizes) {
      Dim minWidth = elementSize._2.getX();
      Dim maxWidth = elementSize._1.calculateSize(minWidth.plus(remainingSpace)).getX();
      if (maxWidth.gt(minWidth)) {
        maxWidths.put(elementSize._1, maxWidth);
      }
    }

    // since we are constructing the widths from 0 below (as opposed to from the delta to the widths that are already taken account by remainingSpace),
    // subtract the widths here
    if (expandableElementSizes.size() > 0) {
      remainingSpace = remainingSpace.plus(Dim.sum(Collections.map(expandableElementSizes, elSize -> elSize._2.getX())));
    }

    // since the maximum width of elements may be different, we iteratively expand all elements until hitting one or more of the elements' maximum widths,
    // then do the same with the remaining elements, and so on, until either we have filled the line or no elements can expand anymore.
    Set<IElement> canExpand = new HashSet<>(maxWidths.keySet());
    Map<IElement, Dim> newWidths = new HashMap<>();
    Dim addedWidth = ZERO;
    while (canExpand.size() > 0 && remainingSpace.gt(screen(0.5f))) { // sub-pixel fill is good enough
      // smallest common width of expandable elements, minus the width we have already added to those
      Dim widthToAdd = Dim.min(Collections.map(Collections.list(canExpand), maxWidths::get)).minus(addedWidth);

      Dim totalAddedWidth = widthToAdd.times(canExpand.size());
      if (totalAddedWidth.gt(remainingSpace)) {
        // scale down - this will be the last pass
        // note: at the moment, we scale everything uniformly, but in the future we could add biases to each element
        widthToAdd = remainingSpace.over(canExpand.size());
      }

      // add onto the widths of elements that can still expand
      Set<IElement> canNoLongerExpand = new HashSet<>();
      for (IElement element : canExpand) {
        if (newWidths.containsKey(element)) {
          newWidths.put(element, newWidths.get(element).plus(widthToAdd));
        } else {
          newWidths.put(element, widthToAdd);
        }

        // this element has reached its maximum expansion
        if (newWidths.get(element).gte(maxWidths.get(element))) {
          canNoLongerExpand.add(element);
        }
      }

      canExpand.removeAll(canNoLongerExpand);

      remainingSpace = remainingSpace.minus(totalAddedWidth);
      addedWidth = addedWidth.plus(widthToAdd);
    }

    // replace sizes in line
    return Collections.map(minimisedLine, line -> {
      IElement element = line._1;
      DimPoint size = line._2;
      if (newWidths.containsKey(element)) {
        size = element.calculateSize(newWidths.get(element));
      }
      return new Tuple2<>(element, size);
    });
  }

  @Override
  protected void renderElement() {
    for (IElement element : this.children) {
      // we also check that the box exists because it's possible the element was not visible at the time of setting the box, but its visibility has since been updated.
      // without the check, it's possible to render the element without first setting its box.
      // this is technically a bug, because this should be a side effect that gets executed *after* the rendering sequence has completed, but I can't figure it out lol
      if (element.getVisible() && element.getBox() != null) {
        element.render(null);
      }
    }
  }

  @Override
  public void setBox(DimRect box) {
    super.setBox(box);

    DimRect contentBox = this.getContentBox();
    for (IElement element : this.children) {
      if (!element.getVisible()) {
        continue;
      }

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

  private static class OffsetResult {
    public final List<Dim> offsets;

    /** True if the alignment is only valid if the reported container size is adhered to. */
    public final boolean requiresContainerSize;

    private OffsetResult(List<Dim> offsets, boolean requiresContainerSize) {
      this.offsets = offsets;
      this.requiresContainerSize = requiresContainerSize;
    }
  }
}
