package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Layout.LayoutGroup;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.MouseEventData;

import javax.annotation.Nullable;
import java.util.List;

/** Adds a boundary layer that redirects propagated (bubbling) interaction events outside of the parent element to the listener.
 * This should be strictly rendered between the main layer and the content layer whose interaction events we want to isolate from the main layer. */
public class InteractionEventBoundaryElement extends SingleElement {
  private final IInteractionEventBoundaryListener listener;
  private final @Nullable List<IElement> holes;

  // parent's children: the element(s) drawn on top of this boundary. any mouse events reaching here that fall outside of these elements are relayed to the listener.
  // holes: the elements whose positions will mark a "hole" in the boundary - mouse events are free to propagate through these (though keyboard events continue to be blocked by the boundary element).
  public InteractionEventBoundaryElement(InteractiveScreen.InteractiveContext context, IElement parent, IInteractionEventBoundaryListener listener, @Nullable List<IElement> holes) {
    super(context, parent);
    super.setLayoutGroup(LayoutGroup.CHILDREN); // it shouldn't interact with siblings

    this.listener = listener;
    this.holes = holes;
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return null;
  }

  @Override
  protected DimPoint calculateThisSize(Dim maxContentSize) {
    return super.context.dimFactory.getMinecraftSize();
  }

  @Override
  public void setBox(DimRect box) {
    // observe the whole screen. in the future we could get a `getRect` function if we want to allow changing this dynamically
    super.setBox(super.context.dimFactory.getMinecraftRect());
  }

  // the element propagation takes advantage of z indexes - since we are the lowest element of the boundary stack,
  // we can override all capture events to simulate a "bottom", and then let events bubble back up.

  @Override
  public void onCaptureMouseDown(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInHole(e.getData().mousePositionData.point)) {
      return;
    }

    e.overrideTarget();
  }

  @Override
  public void onMouseDown(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInBlock(e.getData().mousePositionData.point)) {
      return;
    }

    this.listener.onMouseDown(e);
  }

  @Override
  public void onCaptureMouseMove(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInHole(e.getData().mousePositionData.point)) {
      return;
    }

    e.overrideTarget();
  }

  @Override
  public void onMouseMove(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInBlock(e.getData().mousePositionData.point)) {
      return;
    }

    this.listener.onMouseMove(e);
  }

  @Override
  public void onCaptureMouseUp(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInHole(e.getData().mousePositionData.point)) {
      return;
    }

    e.overrideTarget();
  }

  @Override
  public void onMouseUp(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInBlock(e.getData().mousePositionData.point)) {
      return;
    }

    this.listener.onMouseUp(e);
  }

  @Override
  public void onCaptureMouseScroll(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInHole(e.getData().mousePositionData.point)) {
      return;
    }

    e.overrideTarget();
  }

  @Override
  public void onMouseScroll(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInBlock(e.getData().mousePositionData.point)) {
      return;
    }

    this.listener.onMouseScroll(e);
  }

  @Override
  public void onCaptureKeyDown(InteractiveEvent<KeyboardEventData.In> e) {
    e.overrideTarget();
  }

  @Override
  public void onKeyDown(InteractiveEvent<KeyboardEventData.In> e) {
    this.listener.onKeyDown(e);
  }

  // cancelling this ensures that the MOUSE_ENTER event is fired for all applicable elements on the main layer
  @Override
  public void onCaptureMouseEnter(InteractiveEvent<MouseEventData.In> e) {
    if (this.isInHole(e.getData().mousePositionData.point)) {
      return;
    }

    e.overrideTarget();
  }

  @Override
  protected void renderElement() {
    // not visible
  }

  private boolean isInHole(DimPoint point) {
    if (this.holes == null) {
      return false;
    }

    for (IElement element : this.holes) {
      if (getCollisionBox(element).checkCollision(point)) {
        return true;
      }
    }
    return false;
  }

  private boolean isInBlock(DimPoint point) {
    List<IElement> children = super.parent.getChildren();
    if (children == null) {
      return false;
    }

    for (IElement element : children) {
      if (element != this && getCollisionBox(element).checkCollision(point)) {
        return true;
      }
    }
    return false;
  }

  public interface IInteractionEventBoundaryListener {
    void onMouseDown(InteractiveEvent<MouseEventData.In> e);
    void onMouseMove(InteractiveEvent<MouseEventData.In> e);
    void onMouseUp(InteractiveEvent<MouseEventData.In> e);
    void onMouseScroll(InteractiveEvent<MouseEventData.In> e);

    // note that this is called without relevance to the parent's box - this means that none of the elements above the boundary have stopped propagation of this event
    void onKeyDown(InteractiveEvent<KeyboardEventData.In> e);

    // all other interaction events are target-only
  }
}
