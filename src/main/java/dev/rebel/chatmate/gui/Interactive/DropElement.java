package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractionEventBoundaryElement.IInteractionEventBoundaryListener;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.events.models.MouseEventData;
import dev.rebel.chatmate.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

public class DropElement extends SingleElement implements IInteractionEventBoundaryListener {
  private final IDropElementListener listener;

  private boolean blockInteractionEvents;
  private @Nullable InteractionEventBoundaryElement interactionEventBoundaryElement;
  private @Nullable DimPoint mouseStart;
  private @Nullable DimPoint prevPosition;
  private @Nullable Dim totalDistanceTravelled;

  public DropElement(InteractiveScreen.InteractiveContext context, IElement parent, IDropElementListener listener) {
    super(context, parent);
    super.setZIndex(1); // draw one layer above so we can block the interaction events
    this.listener = listener;

    this.blockInteractionEvents = false;
    this.interactionEventBoundaryElement = null;
    this.mouseStart = context.mousePosition;
    this.prevPosition = context.mousePosition;
    this.totalDistanceTravelled = ZERO;
  }

  /** If true, no mouse events will reach other elements while the element is being dragged. False by default. */
  public DropElement setBlockInteractionEvents(boolean blockInteractionEvents) {
    if (this.blockInteractionEvents != blockInteractionEvents) {
      if (blockInteractionEvents) {
        this.interactionEventBoundaryElement = new InteractionEventBoundaryElement(context, this, this, null);
      } else {
        this.interactionEventBoundaryElement = null;
      }
      super.onInvalidateSize();
    }
    return this;
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return this.interactionEventBoundaryElement == null ? null : Collections.list(this.interactionEventBoundaryElement);
  }

  @Override
  public @Nullable DimRect getVisibleBox() {
    // whole screen should be visible and receive interaction events
    return null;
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    // claim to have no size (so it doesn't interfere with the layout), and set the custom box below
    return this.interactionEventBoundaryElement == null ? new DimPoint(ZERO, ZERO) : this.interactionEventBoundaryElement.calculateSize(maxContentSize);
  }

  @Override
  public void setBox(DimRect box) {
    // observe the whole screen. in the future we could get a `getRect` function if we want to allow changing this dynamically
    super.setBox(super.context.dimFactory.getMinecraftRect());

    if (this.interactionEventBoundaryElement != null) {
      this.interactionEventBoundaryElement.setBox(box);
    }
  }

  @Override
  public void onMouseDown(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.pressedButtons.contains(MouseButton.LEFT_BUTTON)) {
      this.mouseStart = e.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);
      this.prevPosition = this.mouseStart;
    }

    if (this.blockInteractionEvents) {
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseMove(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.pressedButtons.contains(MouseButton.LEFT_BUTTON) && this.prevPosition != null) {
      DimPoint currentPosition = e.getData().mousePositionData.point.setAnchor(DimAnchor.GUI);
      this.totalDistanceTravelled = this.totalDistanceTravelled.plus(currentPosition.distanceTo(this.prevPosition));
      this.listener.onDrag(this.prevPosition, currentPosition);
      this.prevPosition = currentPosition;
    }

    if (this.blockInteractionEvents) {
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseUp(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      this.listener.onDrop(this.mouseStart, e.getData().mousePositionData.point, this.totalDistanceTravelled);
      this.mouseStart = null;
      this.prevPosition = null;
      this.totalDistanceTravelled = null;
    }

    if (this.blockInteractionEvents) {
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseScroll(Events.IEvent<MouseEventData.In> e) {
    if (this.blockInteractionEvents) {
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseExit(Events.IEvent<MouseEventData.In> e) {
    if (this.blockInteractionEvents) {
      e.stopPropagation();
    }
  }

  @Override
  protected void renderElement() {
    if (this.interactionEventBoundaryElement != null) {
      this.interactionEventBoundaryElement.render(null);
    }
  }

  public interface IDropElementListener {
    /** Called when the mouse button was dragged. */
    void onDrag(DimPoint prevPosition, DimPoint currentPosition);
    /** Called when the mouse button was released. */
    void onDrop(DimPoint startPosition, DimPoint endPosition, Dim totalDistanceTravelled);
  }
}
