package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.events.models.MouseEventData.MouseScrollData.ScrollDirection;
import dev.rebel.chatmate.gui.Interactive.DropElement.IDropElementListener;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.StateManagement.AnimatedDim;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.util.Collections;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Acts as a container for the child relement. The scroll bar is added to *this* element, not the children. If you need to add any padding or similar, you must wrap this into another container first. */
public class ScrollingElement extends SingleElement { // use a single element because we have to implement a special layout algorithm, so no point in using the ContainerElement.
  private final static long ANIMATION_DURATION = 300;
  private final static Function<Float, Float> EASING_FUNCTION = frac -> 1 - (float)Math.pow(1 - frac, 5);

  public final ScrollbarElement scrollbarElement;
  private @Nullable IElement childElement;
  private @Nullable Dim maxHeight;
  private ScrollBarLayout scrollBarLayout;
  private @Nullable AnimatedDim scrollingPosition;

  public ScrollingElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.scrollbarElement = new ScrollbarElement(context, this)
        .setVisible(false)
        .setMargin(new RectExtension(gui(1), ZERO, ZERO, ZERO))
        .cast();
    this.childElement = null;
    this.maxHeight = null;
    this.scrollBarLayout = ScrollBarLayout.SIDEBAR;
    this.scrollingPosition = null;
  }

  public ScrollingElement setElement(@Nullable IElement element) {
    if (this.childElement != element) {
      this.childElement = element;
      this.childElement.setParent(this);
    }
    return this;
  }

  /** Gets the underlying element. */
  public @Nullable IElement getElement() {
    return this.childElement;
  }

  /** Start scrolling once the element's height exceeds this value. */
  public ScrollingElement setMaxHeight(@Nullable Dim maxHeight) {
    if (!Objects.equals(this.maxHeight, maxHeight)) {
      this.maxHeight = maxHeight;
      super.onInvalidateSize();
    }
    return this;
  }

  private void updateScrollingPosition() {
    Dim contentHeight = this.childElement.getLastCalculatedSize().getY();
    boolean scrollingEnabled = this.maxHeight != null && contentHeight.gt(this.maxHeight);
    this.scrollbarElement.setVisible(scrollingEnabled);

    if (scrollingEnabled) {
      if (this.scrollingPosition == null) {
        this.scrollingPosition = new AnimatedDim(ANIMATION_DURATION, ZERO);
        this.scrollingPosition.setEasing(EASING_FUNCTION);
        super.onInvalidateSize();

      } else {
        // only modify the scrolling position if it exceeds the maximum
        Dim maxScrollingPosition = this.getMaxScrollingPosition();
        if (this.scrollingPosition.getTarget().gt(maxScrollingPosition)) {
          this.scrollingPosition.setImmediate(maxScrollingPosition);
          super.onInvalidateSize();
        }
      }

    } else {
      if (this.scrollingPosition != null) {
        this.scrollingPosition = null;
        super.onInvalidateSize();
      }
    }
  }

  private Dim getMaxScrollingPosition() {
    return this.childElement.getLastCalculatedSize().getY().minus(this.maxHeight);
  }

  @Override
  public void onMouseScroll(InteractiveEvent<MouseEventData> e) {
    if (this.scrollingPosition == null || this.scrollbarElement.isDragging()) {
      return;
    }

    float strength = super.context.keyboardEventService.isHeldDown(Keyboard.KEY_LSHIFT) ? 3f : 20;

    ScrollDirection scrollDirection = e.getData().mouseScrollData.scrollDirection;
    Dim delta = gui(strength).times(scrollDirection == ScrollDirection.DOWN ? 1 : -1);
    Dim scrollingPosition = this.scrollingPosition.getTarget().plus(delta);

    Dim maxScrollingPosition = this.getMaxScrollingPosition();
    if (scrollingPosition.lt(ZERO)) {
      scrollingPosition = ZERO;
    } else if (scrollingPosition.gt(maxScrollingPosition)) {
      scrollingPosition = maxScrollingPosition;
    }

    if (!scrollingPosition.equals(this.scrollingPosition.getTarget())) {
      this.scrollingPosition.set(scrollingPosition);
      super.onInvalidateSize();
    }

    e.stopPropagation();
  }

  @Override
  public List<IElement> getChildren() {
    return Collections.filter(Collections.list(this.scrollbarElement, this.childElement), Objects::nonNull);
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    if (this.childElement == null) {
      return new DimPoint(ZERO, ZERO);
    }

    DimPoint fullChildSize = this.childElement.calculateSize(maxContentSize);
    if (this.maxHeight != null && fullChildSize.getY().gt(this.maxHeight)) {
      DimPoint barSize = this.scrollbarElement.calculateSize(maxContentSize);
      Dim maxChildSize = maxContentSize.minus(barSize.getX()); // make room for the scroll bar
      DimPoint childSize = this.childElement.calculateSize(maxChildSize);
      return new DimPoint(childSize.getX().plus(barSize.getX()), this.maxHeight);
    } else {
      this.scrollingPosition = null;
      return fullChildSize;
    }
  }

  @Override
  public void setBox(DimRect box) {
    this.updateScrollingPosition();

    super.setBox(box);
    if (this.scrollingPosition != null && this.childElement != null) {
      super.setVisibleBox(super.getContentBox());

      // give the child its full box, but translate it so that only the scrolled portion overlaps with our own content box.
      // this is better than doing a GL transform because we don't need to worry about applying the transform to interactions as well
      DimRect childBox = super.getContentBox()
          .withTranslation(new DimPoint(ZERO, this.scrollingPosition.get().times(-1))) // we are moving the box up to scroll down
          .withWidth(this.childElement.getLastCalculatedSize().getX())
          .withHeight(this.childElement.getLastCalculatedSize().getY());
      this.childElement.setBox(childBox);

      DimRect barBox = super.getContentBox()
          .withTranslation(new DimPoint(childBox.getWidth(), ZERO)) // scrollbar is to the right of the child
          .withSize(this.scrollbarElement.getLastCalculatedSizeOrZero());
      this.scrollbarElement.setBox(barBox);

    } else if (this.childElement != null) {
      this.childElement.setBox(super.getContentBox());
    }
  }

  @Override
  protected void renderElement() {
    if (this.childElement == null) {
      return;
    }

    // animate the contents
    if (this.scrollingPosition != null && this.scrollingPosition.getFrac() < 1) {
      super.onInvalidateSize();
    }

    this.childElement.render(onRender -> RendererHelpers.withScissor(super.getContentBox(), super.context.dimFactory.getMinecraftSize(), onRender));
    if (this.scrollbarElement.getVisible()) {
      this.scrollbarElement.render(null);
    }
  }

  public enum ScrollBarLayout {
    /** The scroll bar is rendered next to the child (on the right), in the right border region of this element. */
    SIDEBAR,
    /** The scroll bar is rendered on top of the child. */ // todo: implement this layout mode
//    OVERLAY
  }

  private class ScrollbarElement extends SingleElement implements IDropElementListener {
    private final Dim width;
    private final Dim minBarHeight;

    private @Nullable DropElement dropElement;
    private @Nullable DimPoint dragPositionStart;
    private @Nullable Dim scrollingPositionStart;

    public ScrollbarElement(InteractiveContext context, IElement parent) {
      super(context, parent);

      this.width = gui(2);
      this.minBarHeight = gui(6);

      this.dropElement = null;
      this.dragPositionStart = null;
      this.scrollingPositionStart = null;
    }

    public boolean isDragging() {
      return this.dropElement != null;
    }
    
    /** Returns the draggable rect of the scroll region. */
    private DimRect getBarRect() {
      Dim totalHeight = ScrollingElement.this.childElement.getBox().getHeight();
      Dim visibleHeight = ScrollingElement.this.maxHeight;
      float ratioVisible = visibleHeight.over(totalHeight);
      DimRect contentRect = super.getContentBox();
      Dim barHeight = Dim.max(this.minBarHeight, contentRect.getHeight().times(ratioVisible));

      Dim scrollingPosition = ScrollingElement.this.scrollingPosition.get(); // in child space
      Dim maxScrollingPosition = ScrollingElement.this.getMaxScrollingPosition();
      float scrollingRatio = scrollingPosition.over(maxScrollingPosition);
      Dim barY = contentRect.getHeight().minus(barHeight).times(scrollingRatio); // in scrollbar space

      return new DimRect(contentRect.getX(), contentRect.getY().plus(barY), this.width, barHeight);
    }

    @Override
    public void onMouseDown(InteractiveEvent<MouseEventData> e) {
      DimPoint position = e.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);
      if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON && this.getBarRect().checkCollision(position)) {
        this.dragPositionStart = position;
        this.dropElement = new DropElement(super.context, this, this)
            .setBlockInteractionEvents(true);
        this.scrollingPositionStart = ScrollingElement.this.scrollingPosition.getTarget();
        super.onInvalidateSize();
        e.stopPropagation();
      }
    }

    @Override
    public void onDrag(DimPoint prevPosition, DimPoint currentPosition) {
      AnimatedDim scrollingPosition = ScrollingElement.this.scrollingPosition;
      Dim maxScrollingPosition = ScrollingElement.this.getMaxScrollingPosition();

      Dim deltaMouse = currentPosition.getY().minus(this.dragPositionStart.getY());
      float deltaRatio = deltaMouse.over(super.getContentBox().getHeight());
      Dim deltaContent = ScrollingElement.this.childElement.getBox().getHeight().times(deltaRatio);
      Dim newTarget = Dim.clamp(this.scrollingPositionStart.plus(deltaContent), ZERO, maxScrollingPosition);
      Dim currentTarget = scrollingPosition.getTarget();

      scrollingPosition.translate(newTarget.minus(currentTarget));
      super.onInvalidateSize();
    }

    @Override
    public void onDrop(DimPoint startPosition, DimPoint currentPosition, Dim totalDistanceTravelled) {
      this.dragPositionStart = null;
      this.dropElement = null;
      this.scrollingPositionStart = null;
      super.onInvalidateSize();
    }

    @Override
    public @Nullable List<IElement> getChildren() {
      return this.dropElement == null ? null : Collections.list(this.dropElement);
    }

    @Override
    protected DimPoint calculateThisSize(Dim maxContentSize) {
      if (ScrollingElement.this.maxHeight == null) {
        return new DimPoint(ZERO, ZERO);
      }

      return new DimPoint(this.width, ScrollingElement.this.maxHeight);
    }

    @Override
    public void setBox(DimRect box) {
      super.setBox(box);

      if (this.dropElement != null) {
        this.dropElement.setBox(box);
      }
    }

    @Override
    protected void renderElement() {
      DimRect bar = this.getBarRect();
      Dim cornerRadius = gui(1);

      Colour colour;
      if (this.isDragging()) {
        colour = Colour.GREY33;
      } else if (this.getBarRect().checkCollision(super.context.mouseEventService.getCurrentPosition().point)) {
        colour = Colour.GREY50;
      } else {
        colour = Colour.GREY67;
      }

      // since this is a child of the ScrollingElement, it is rendered outside the scissor region
      RendererHelpers.withoutScissor(() -> {
        RendererHelpers.drawRect(this.getEffectiveZIndex(), bar, colour, null, null, cornerRadius);
      });
    }
  }
}
