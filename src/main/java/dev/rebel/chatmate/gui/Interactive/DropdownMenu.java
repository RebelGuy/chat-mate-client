package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// note: this overlays onto the entire screen and steals input events until closed or clicked away. to reuse this, the
// following refactoring may be required:
// - DropdownMenu extends OverlayElement, and all the input overriding logic is moved there
// - The list of option elements is passed to the OverlayElement, and the overlay element will relay mouse events to those
//   children similar to how the Element engine currently does it
// - essentially, it will be an isolated "mini interactive screen" until it's closed
// - the f3 debug menu (which currently has would recognise only the clickaway region and children element, and it should never be possible
//   to navigate to any other elements.

/** Attaches itself to the specified parent. */
public class DropdownMenu extends ContainerElement {
  private final Dim padding;
  private final IElement anchorElement;

  private List<Tuple2<LabelElement, Runnable>> options;
  private boolean expanded;
  private float fontScale;
  private DimPoint optionsSize;
  private @Nullable Anchor anchor;
  private SizingMode sizingMode;

  /** The full box of the visible dropdown menu. */
  private DimRect dropdownBox;

  public DropdownMenu(InteractiveContext context, IElement anchor) {
    super(context, anchor, LayoutMode.BLOCK);

    this.padding = gui(2);
    this.anchorElement = anchor;
    this.sizingMode = SizingMode.MINIMISE;

    this.options = new ArrayList<>();
    this.expanded = false;
    this.fontScale = 1;
    this.setZIndex(10);
  }

  public DropdownMenu addOption(String name, Runnable onSelect) {
    LabelElement label = new LabelElement(this.context, this)
        .setText(name)
        .setFontScale(this.fontScale)
        .setOverflow(TextOverflow.SPLIT)
        .setPadding(new RectExtension(this.padding))
        .cast();
    this.options.add(new Tuple2<>(label, onSelect));
    super.addElement(label);
    return this;
  }

  public DropdownMenu setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      this.onInvalidateSize();
    }
    return this;
  }

  public DropdownMenu toggleExpanded() {
    return this.setExpanded(!this.expanded);
  }

  public DropdownMenu setAnchor(Anchor anchor) {
    this.anchor = anchor;
    return this;
  }

  public DropdownMenu setFontScale(float fontScale) {
    this.fontScale = fontScale;
    this.options.forEach(op -> op._1.setFontScale(fontScale));
    return this;
  }

  @Override
  public void onCaptureMouseDown(IEvent<In> e) {
    DimPoint point = e.getData().mousePositionData.point;
    if (!this.expanded || getCollisionBox(this.anchorElement).checkCollision(point)) {
      return;
    }

    for (Tuple2<LabelElement, Runnable> option : this.options) {
      LabelElement label = option._1;
      if (label.getCollisionBox().checkCollision(point)) {
        option._2.run();
        break;
      }
    }

    this.setExpanded(false);
    e.stopPropagation();
  }

  @Override
  public void onCaptureMouseMove(IEvent<In> e) {
    if (this.expanded && !getCollisionBox(this.anchorElement).checkCollision(e.getData().mousePositionData.point)) {
      e.stopPropagation();
    }
  }

  @Override
  public void onCaptureMouseEnter(IEvent<In> e) {
    if (this.expanded && !getCollisionBox(this.anchorElement).checkCollision(e.getData().mousePositionData.point)) {
      e.stopPropagation();
    }
  }

  @Override
  public void onCaptureMouseScroll(IEvent<In> e) {
    if (this.expanded && !getCollisionBox(this.anchorElement).checkCollision(e.getData().mousePositionData.point)) {
      e.stopPropagation();
    }
  }

  @Override
  public void onCaptureMouseUp(IEvent<In> e) {
    if (this.expanded && !getCollisionBox(this.anchorElement).checkCollision(e.getData().mousePositionData.point)) {
      e.stopPropagation();
    }
  }

  @Override
  public void onCaptureKeyDown(IEvent<KeyboardEventData.In> e) {
    if (this.expanded) {
      e.stopPropagation();
    }
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    // the element does not take up any space in the layout-space
    // (note that the super method is called in `setBox()`)
    return new DimPoint(ZERO, ZERO);
  }

  @Override
  public IElement setSizingMode(SizingMode sizingMode) {
    this.sizingMode = sizingMode;
    return this;
  }

  @Override
  public SizingMode getSizingMode() {
    return this.sizingMode;
  }

  @Override
  public void setBox(DimRect box) {
    if (!this.expanded) {
      super.setBox(box);
      return;
    }

    // since we have provided a size of zero, we need to override the box completely
    Anchor effectiveAnchor;
    if (this.anchor == null) {
      HorizontalAlignment alignment = this.anchorElement.getHorizontalAlignment();
      if (alignment == HorizontalAlignment.LEFT || alignment == HorizontalAlignment.CENTRE) {
        effectiveAnchor = Anchor.LEFT;
      } else if (alignment == HorizontalAlignment.RIGHT) {
        effectiveAnchor = Anchor.RIGHT;
      } else {
        throw new RuntimeException("Invalid horizontal alignment " + alignment);
      }
    } else {
      effectiveAnchor = this.anchor;
    }

    // we can't set this in `calculateThisSize()` because the anchor element may not have had their size set at that point yet.
    Dim maxWidth = this.anchorElement.getBox().getWidth();
    DimPoint size = super.calculateThisSize(maxWidth);
    if (this.getSizingMode() == SizingMode.FILL && maxWidth.gt(size.getX())) {
      size = new DimPoint(this.anchorElement.getBox().getWidth(), size.getY());
    }

    Dim x;
    if (effectiveAnchor == Anchor.LEFT) {
      x = this.anchorElement.getBox().getX();
    } else {
      x = this.anchorElement.getBox().getRight().minus(size.getX());
    }
    Dim y = this.anchorElement.getBox().getBottom();

    this.dropdownBox = new DimRect(new DimPoint(x, y), super.getFullBoxSize(size));
    super.setBox(this.dropdownBox); // the "official" box for rendering the content
    super.setBoxUnsafe(this.context.dimFactory.getMinecraftRect()); // hack: stop any input events from propagating to lower elements
  }

  private void highlightLabelAtPosition(DimPoint position) {
    for (Tuple2<LabelElement, Runnable> option : this.options) {
      LabelElement label = option._1;
      if (label.getContentBox().checkCollision(position)) {
        label.setColour(Colour.LTGREY);
      } else {
        label.setColour(Colour.WHITE);
      }
    }
  }

  @Override
  public void renderElement() {
    if (!this.expanded) {
      return;
    }

    Colour background = new Colour(0, 0, 0, 127); // dark gray
    Dim borderSize = Dim.max(this.getBorder().left, this.getBorder().right, this.getBorder().top, this.getBorder().bottom);
    Dim cornerRadius = gui(0);
    Dim shadowDistance = gui(1);
    DimRect paddingBox = super.getMargin().plus(super.getBorder()).applySubtractive(this.dropdownBox);
    RendererHelpers.drawRect(0, paddingBox, background, borderSize, Colour.BLACK, cornerRadius, shadowDistance, Colour.BLACK);

    this.highlightLabelAtPosition(this.context.mousePosition);
    super.renderElement();
  }

  /** On which side of the anchor element to pin this menu to. If not set, uses the element's alignment. */
  public enum Anchor { LEFT, RIGHT }
}
