package dev.rebel.chatmate.gui.Interactive;

// the mouse event seems to consider every element separately to check for collisions, regardless of parent sizes. this is good, because it allows us to render the overlay element
// at a height z index and can use the existing events (but we need to make sure they never pass through the layer to normal elements - must be confined to the overlay element and children)

import dev.rebel.chatmate.gui.Interactive.InteractionEventBoundaryElement.IInteractionEventBoundaryListener;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.LayoutGroup;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nullable;
import java.util.List;

public class OverlayElement extends ContainerElement implements IInteractionEventBoundaryListener {
  private final InteractionEventBoundaryElement interactionEventBoundaryElement;

  public OverlayElement(InteractiveContext context, IElement parent, LayoutMode mode, @Nullable List<IElement> holes) {
    super(context, parent, mode);
    super.setLayoutGroup(LayoutGroup.CHILDREN);

    // this layer is above the main layer
    super.setZIndex(1);

    // all other children will be rendered one layer above the boundary element
    this.interactionEventBoundaryElement = new InteractionEventBoundaryElement(context, this, this, holes);

    // note that this will be an "initial element" and won't ever get cleared
    super.addElement(this.interactionEventBoundaryElement);
  }

  @Override
  public ContainerElement addElement(IElement element) {
    if (element instanceof InteractionEventBoundaryElement) {
      return super.addElement(element.setZIndex(1));
    } else {
      return super.addElement(element.setZIndex(2));
    }
  }

  // interaction events bubbling up to here haven't been handled by our overlay children. stop them from propagating to the main layer
  @Override
  public void onMouseDown(Events.IEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onMouseMove(Events.IEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onMouseUp(Events.IEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onMouseScroll(Events.IEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onKeyDown(Events.IEvent<KeyboardEventData.In> e) {
    e.stopPropagation();
  }
}
