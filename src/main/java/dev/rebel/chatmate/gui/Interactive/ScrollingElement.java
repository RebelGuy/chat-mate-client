package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.services.util.Collections;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/** Acts as a container for the child relement. The scroll bar is added to *this* element, not the children. If you need to add any padding or similar, you must wrap this into another container first. */
public class ScrollingElement extends SingleElement {
  private @Nullable IElement childElement;
  private @Nullable Dim maxHeight;
  private ScrollBarLayout scrollBarLayout;
  private @Nullable Scrollbar scrollbar;

  public ScrollingElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    // super.setBorder(new RectExtension(ZERO, gui(4), ZERO, ZERO)); // todo: only set border when the scrollbar is visible
    this.childElement = null;
    this.maxHeight = null;
    this.scrollBarLayout = ScrollBarLayout.SIDEBAR;
    this.scrollbar = null;
  }

  public ScrollingElement setElement(@Nullable IElement element) {
    if (this.childElement != element) {
      this.childElement = element;
      this.childElement.setParent(this);
      super.onInvalidateSize();
    }
    return this;
  }

  /** Start scrolling once the element's height exceeds this value. */
  public ScrollingElement setMaxHeight(@Nullable Dim maxHeight) {
    if (!Objects.equals(this.maxHeight, maxHeight)) {
      this.maxHeight = maxHeight;
      super.onInvalidateSize();
    }
    return this;
  }

  private void setScrollbar(Dim contentHeight, Dim maxHeight) {
    Dim scrollingPosition;
    if (this.scrollbar == null) {
      scrollingPosition = ZERO;
      super.onInvalidateSize();
    } else {
      if (this.scrollbar.scrollingPosition.plus(maxHeight).gt(contentHeight)) {
        scrollingPosition = contentHeight.minus(maxHeight); // max scrolling position
      } else {
        scrollingPosition = this.scrollbar.scrollingPosition;
      }
    }

    this.scrollbar = new Scrollbar(contentHeight, maxHeight, scrollingPosition);
  }

  private void removeScrollbar() {
    if (this.scrollbar != null) {
      super.onInvalidateSize();
    }
    this.scrollbar = null;
  }

  @Override
  public void onMouseDown(IEvent<MouseEventData.In> e) {
    DimPoint position = e.getData().mousePositionData.point;
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {

      e.stopPropagation();
    }
  }

  @Override
  public void onMouseMove(IEvent<MouseEventData.In> e) {
    DimPoint position = e.getData().mousePositionData.point;
    if (e.getData().mouseButtonData.pressedButtons.contains(MouseButton.LEFT_BUTTON)) {

      e.stopPropagation();
    }
  }

  @Override
  public void onMouseUp(IEvent<MouseEventData.In> e) {
    DimPoint position = e.getData().mousePositionData.point;
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {

      e.stopPropagation();
    }
  }

  @Override
  public void onMouseScroll(IEvent<MouseEventData.In> e) {
    if (this.scrollbar == null) {
      return;
    }

    float strength = super.context.keyboardEventService.isHeldDown(Keyboard.KEY_LSHIFT) ? 1 : 8;

    ScrollDirection scrollDirection = e.getData().mouseScrollData.scrollDirection;
    Dim delta = gui(strength).times(scrollDirection == ScrollDirection.DOWN ? 1 : -1);
    Dim scrollingPosition = this.scrollbar.scrollingPosition.plus(delta);

    if (scrollingPosition.lt(ZERO)) {
      scrollingPosition = ZERO;
    } else if (scrollingPosition.gt(this.scrollbar.contentHeight.minus(this.scrollbar.maxHeight))) {
      scrollingPosition = this.scrollbar.contentHeight.minus(this.scrollbar.maxHeight);
    }

    if (!scrollingPosition.equals(this.scrollbar.scrollingPosition)) {
      this.scrollbar = new Scrollbar(this.scrollbar.contentHeight, this.scrollbar.maxHeight, scrollingPosition);
      super.onInvalidateSize();
    }

    e.stopPropagation();
  }

  @Override
  public List<IElement> getChildren() {
    return this.childElement == null ? null : Collections.list(this.childElement);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    if (this.childElement == null) {
      return new DimPoint(ZERO, ZERO);
    }

    DimPoint fullChildSize = this.childElement.calculateSize(maxContentSize);
    if (this.maxHeight != null && fullChildSize.getY().gt(this.maxHeight)) {
      this.setScrollbar(fullChildSize.getY(), this.maxHeight);
      return new DimPoint(fullChildSize.getX(), this.maxHeight);
    } else {
      this.removeScrollbar();
      return fullChildSize;
    }
  }

  @Override
  public void setBox(DimRect box) {
    if (this.scrollbar == null || this.childElement == null) {
      super.setBox(box);
    } else {
      super.setBoxUnsafe(box); // this box is only for this element
      super.setVisibleBox(super.getContentBox());

      // give the child its full box, but translate it so that only the scrolled portion overlaps with our own content box.
      // this is better than doing a GL transform because we don't need to worry about applying the transform to interactions as well
      DimRect childBox = super.getContentBox()
          .withTranslation(new DimPoint(ZERO, this.scrollbar.scrollingPosition.times(-1))) // we are moving the box up to scroll down
          .withHeight(this.childElement.getLastCalculatedSize().getY());
      this.childElement.setBox(childBox);
    }
  }

  @Override
  protected void renderElement() {
    if (this.childElement == null) {
      return;
    }

    this.childElement.render(onRender -> RendererHelpers.withScissor(super.getContentBox(), super.context.dimFactory.getMinecraftSize(), onRender));
    // todo: render scroll bar
  }

  public enum ScrollBarLayout {
    /** The scroll bar is rendered next to the child (on the right), in the right border region of this element. */
    SIDEBAR,
    /** The scroll bar is rendered on top of the child. */ // todo: implement this layout mode
//    OVERLAY
  }

  private static class Scrollbar {
    public final Dim scrollingPosition; // from top
    public final Dim contentHeight;
    public final Dim maxHeight;

    public Scrollbar(Dim contentHeight, Dim maxHeight, Dim scrollingPosition) {
      this.scrollingPosition = scrollingPosition;
      this.contentHeight = contentHeight;
      this.maxHeight = maxHeight;
    }
  }
}
