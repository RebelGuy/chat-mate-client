package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.EnumHelpers;

/** Attaches itself to the specified parent. */
public class DropdownMenu extends OverlayElement {
  private final BlockElement itemsElement;
  private final ScrollingElement scrollingContainer;
  private final AnchorBoxSizing anchorBoxSizing;

  private IElement anchorElement;
  private HorizontalPosition horizontalPosition;
  private VerticalPosition verticalPosition;
  private Colour background;

  public DropdownMenu(InteractiveContext context, IElement anchor, AnchorBoxSizing anchorBoxSizing) {
    super(context, anchor, LayoutMode.BLOCK, Collections.list(anchor));
    super.setVisible(false);

    this.anchorElement = anchor;
    this.anchorBoxSizing = anchorBoxSizing;
    this.horizontalPosition = HorizontalPosition.LEFT;
    this.verticalPosition = VerticalPosition.BELOW;
    this.background = new Colour(0, 0, 0, 127); // dark gray

    this.itemsElement = new BlockElement(context, this)
        .setSizingMode(SizingMode.FILL) // so `block` hitboxes in the event boundary are consistent with the drawn background
        .cast();
    this.scrollingContainer = new ScrollingElement(context, this)
        .setElement(this.itemsElement);
    super.addElement(this.scrollingContainer);
  }

  public DropdownMenu addOption(IElement element) {
    this.itemsElement.addElement(element);
    return this;
  }

  public DropdownMenu clearOptions() {
    this.itemsElement.clear();
    return this;
  }

  public DropdownMenu toggleVisible() {
    return this.setVisible(!this.getVisible()).cast();
  }

  public DropdownMenu setHorizontalPosition(HorizontalPosition horizontalPosition) {
    if (this.horizontalPosition != horizontalPosition) {
      this.horizontalPosition = horizontalPosition;
      super.onInvalidateSize();
    }
    return this;
  }

  public DropdownMenu setVerticalPosition(VerticalPosition verticalPosition) {
    if (this.verticalPosition != verticalPosition) {
      this.verticalPosition = verticalPosition;
      super.onInvalidateSize();
    }
    return this;
  }

  public DropdownMenu setBackground(Colour background) {
    this.background = background;
    return this;
  }

  /** Updates the element to which this dropdown menu is anchored, respecting the `AnchorBoxSizing` that is set. */
  public DropdownMenu setAnchorElement(IElement element) {
    if (this.anchorElement != element) {
      this.anchorElement = element;
      super.onInvalidateSize();
    }

    return this;
  }

  // clickaway listener
  @Override
  public void onMouseDown(InteractiveEvent<MouseEventData> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      e.stopPropagation();
      super.setVisible(false);
    }
  }

  private DimRect getAnchorBox() {
    if (this.anchorBoxSizing == AnchorBoxSizing.FULL) {
      return this.anchorElement.getBox();
    } else if (this.anchorBoxSizing == AnchorBoxSizing.BORDER) {
      return getBorderBox(this.anchorElement);
    } else if (this.anchorBoxSizing == AnchorBoxSizing.PADDING) {
      return getPaddingBox(this.anchorElement);
    } else if (this.anchorBoxSizing == AnchorBoxSizing.CONTENT) {
      return getContentBox(this.anchorElement);
    } else {
      throw EnumHelpers.<AnchorBoxSizing>assertUnreachable(this.anchorBoxSizing);
    }
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    // anchor must be initialised first
    if (this.anchorElement.getLastCalculatedSize() == null) {
      super.onInvalidateSize();
      return new DimPoint(ZERO, ZERO);
    }

    Dim maxWidth = this.anchorElement.getLastCalculatedSize().getX();
    DimPoint size = super.calculateThisSize(maxWidth);
    if (this.getSizingMode() == SizingMode.FILL && maxWidth.gt(size.getX())) {
      size = new DimPoint(maxWidth, size.getY());
    }

    return size;
  }

  @Override
  public void setBox(DimRect _box) {
    // the box won't be positioned where we want it (its placement was determined by the normal Container layout algorithm), but it will have the correct size
    DimPoint size = super.lastCalculatedSize;

    Dim x;
    if (this.horizontalPosition == HorizontalPosition.LEFT) {
      x = this.getAnchorBox().getLeft();
    } else {
      x = this.getAnchorBox().getRight().minus(size.getX());
    }

    Dim y;
    DimRect fullAnchorBox = this.anchorElement.getBox(); // we are setting the full box, so this guarantees that there is no vertical clipping between the anchor and dropdown boxes
    if (this.verticalPosition == VerticalPosition.BELOW) {
      y = fullAnchorBox.getBottom();
    } else {
      y = fullAnchorBox.getTop().minus(size.getY());
    }

    DimRect box = new DimRect(new DimPoint(x, y), size);

    // limit the height of the dropdown menu to prevent it from clipping outside the visible area
    DimRect visibleRect = super.getVisibleBox();
    if (visibleRect != null) {
      visibleRect = new RectExtension(gui(4)).applySubtractive(visibleRect);
      Dim maxHeight;
      if (this.verticalPosition == VerticalPosition.BELOW) {
        maxHeight = visibleRect.getBottom().minus(fullAnchorBox.getBottom());
      } else {
        maxHeight = fullAnchorBox.getTop().minus(visibleRect.getTop());
      }
      this.scrollingContainer.setMaxHeight(maxHeight);
    }

    super.setBox(box);
  }

  @Override
  protected void renderElement() {
    if (this.itemsElement.children.size() == 0) {
      return;
    }

    Dim borderSize = Dim.max(this.getBorder().left, this.getBorder().right, this.getBorder().top, this.getBorder().bottom);
    Dim cornerRadius = gui(1);
    Dim shadowDistance = gui(1);
    DimRect paddingBox = super.getPaddingBox();
    RendererHelpers.drawRect(0, paddingBox, this.background, borderSize, Colour.BLACK, cornerRadius, shadowDistance, Colour.BLACK);

    super.renderElement();
  }

  /** On which side of the anchor element to pin this menu to. Defaults to left. */
  public enum HorizontalPosition { LEFT, RIGHT }

  /** Whether to pin this menu to above or below the anchor menu. Defaults to below. */
  public enum VerticalPosition { BELOW, ABOVE }

  /** Which box of the anchor element to use when positioning the dropdown menu and calculating its size. */
  public enum AnchorBoxSizing { FULL, BORDER, PADDING, CONTENT }
}
