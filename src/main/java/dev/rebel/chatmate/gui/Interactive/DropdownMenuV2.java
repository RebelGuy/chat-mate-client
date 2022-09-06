package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.EnumHelpers;
import dev.rebel.chatmate.services.util.Objects;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

/** Attaches itself to the specified parent. */
public class DropdownMenuV2 extends OverlayElement {
  private final IElement anchorElement;
  private final BlockElement itemsElement;
  private final AnchorBoxSizing anchorBoxSizing;

  private HorizontalPosition horizontalPosition;
  private VerticalPosition verticalPosition;

  public DropdownMenuV2(InteractiveContext context, IElement anchor, AnchorBoxSizing anchorBoxSizing) {
    super(context, anchor, LayoutMode.BLOCK, Collections.list(anchor));
    super.setVisible(false);

    this.anchorElement = anchor;
    this.anchorBoxSizing = anchorBoxSizing;
    this.horizontalPosition = HorizontalPosition.LEFT;
    this.verticalPosition = VerticalPosition.BELOW;
    this.itemsElement = new BlockElement(context, this)
        .setSizingMode(SizingMode.FILL) // so `block` hitboxes in the event boundary are consistent with the drawn background
        .cast();
    super.addElement(this.itemsElement);
  }

  public DropdownMenuV2 addOption(IElement element) {
    this.itemsElement.addElement(element);
    return this;
  }

  public DropdownMenuV2 clearOptions() {
    this.itemsElement.clear();
    return this;
  }

  public DropdownMenuV2 toggleVisible() {
    return this.setVisible(!this.getVisible()).cast();
  }

  public DropdownMenuV2 setHorizontalPosition(HorizontalPosition horizontalPosition) {
    if (this.horizontalPosition != horizontalPosition) {
      this.horizontalPosition = horizontalPosition;
      super.onInvalidateSize();
    }
    return this;
  }

  public DropdownMenuV2 setVerticalPosition(VerticalPosition verticalPosition) {
    if (this.verticalPosition != verticalPosition) {
      this.verticalPosition = verticalPosition;
      super.onInvalidateSize();
    }
    return this;
  }

  @Override
  public void onMouseDown(IEvent<In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      e.stopPropagation();
      super.setVisible(false);
    }
  }

  // todo: this doesn't fire. something with how we are getting the chidlren in the interactive screen when propagating keyboard events
  @Override
  public void onKeyDown(IEvent<KeyboardEventData.In> e) {
    if (e.getData().isPressed(Keyboard.KEY_ESCAPE)) {
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
    if (this.anchorElement.getBox() == null) {
      super.onInvalidateSize();
      return new DimPoint(ZERO, ZERO);
    }

    Dim maxWidth = this.getAnchorBox().getWidth();
    DimPoint size = super.calculateThisSize(maxWidth);
    if (this.getSizingMode() == SizingMode.FILL && maxWidth.gt(size.getX())) {
      size = new DimPoint(maxWidth, size.getY());
    }

    return size;
  }

  @Override
  public void setBox(DimRect box) {
    // the box won't be positioned where we want it (its placement was determined by the normal Container layout algorithm), but it will have the correct size
    DimPoint size = super.lastCalculatedSize;

    Dim x;
    if (this.horizontalPosition == HorizontalPosition.LEFT) {
      x = this.getAnchorBox().getX();
    } else {
      x = this.getAnchorBox().getRight().minus(size.getX());
    }

    Dim y;
    DimRect fullAnchorBox = this.anchorElement.getBox(); // we are setting the full box, so this guarantees that there is no vertical clipping between the anchor and dropdown boxes
    if (this.verticalPosition == VerticalPosition.BELOW) {
      y = fullAnchorBox.getBottom();
    } else {
      y = fullAnchorBox.getY().minus(size.getY());
    }

    super.setBox(new DimRect(new DimPoint(x, y), size));
  }

  @Override
  protected void renderElement() {
    if (this.itemsElement.children.size() == 0) {
      return;
    }

    Colour background = new Colour(0, 0, 0, 127); // dark gray
    Dim borderSize = Dim.max(this.getBorder().left, this.getBorder().right, this.getBorder().top, this.getBorder().bottom);
    Dim cornerRadius = gui(1);
    Dim shadowDistance = gui(1);
    DimRect paddingBox = super.getPaddingBox();
    RendererHelpers.drawRect(0, paddingBox, background, borderSize, Colour.BLACK, cornerRadius, shadowDistance, Colour.BLACK);

    super.renderElement();
  }

  /** On which side of the anchor element to pin this menu to. Defaults to left. */
  public enum HorizontalPosition { LEFT, RIGHT }

  /** Whether to pin this menu to above or below the anchor menu. Defaults to below. */
  public enum VerticalPosition { BELOW, ABOVE }

  /** Which box of the anchor element to use when positioning the dropdown menu and calculating its size. */
  public enum AnchorBoxSizing { FULL, BORDER, PADDING, CONTENT }
}
