package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractionEventBoundaryElement.IInteractionEventBoundaryListener;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.SingleElement;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

public class DropElement extends SingleElement implements IInteractionEventBoundaryListener {
  private final IDropElementListener listener;
  private final boolean blockInteractionEvents;
  private final @Nullable InteractionEventBoundaryElement interactionEventBoundaryElement;

  public DropElement(InteractiveScreen.InteractiveContext context, IElement parent, boolean blockInteractionEvents, IDropElementListener listener) {
    super(context, parent);
    super.setZIndex(1); // draw one layer above so we can block the interaction events
    this.listener = listener;
    this.blockInteractionEvents = blockInteractionEvents;

    if (this.blockInteractionEvents) {
      this.interactionEventBoundaryElement = new InteractionEventBoundaryElement(context, this, this, null);
    } else {
      this.interactionEventBoundaryElement = null;
    }
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
    if (this.blockInteractionEvents) {
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseMove(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.pressedButtons.contains(MouseButton.LEFT_BUTTON)) {
      this.listener.onDrag(e.getData().mousePositionData.point);
      e.stopPropagation();
    }

    if (this.blockInteractionEvents) {
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseUp(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      this.listener.onDrop(e.getData().mousePositionData.point);
      e.stopPropagation();
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
    void onDrag(DimPoint position);
    /** Called when the mouse button was released. */
    void onDrop(DimPoint position);
  }
}
