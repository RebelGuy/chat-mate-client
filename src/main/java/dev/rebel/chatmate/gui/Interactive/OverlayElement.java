package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.InteractionEventBoundaryElement.IInteractionEventBoundaryListener;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.LayoutGroup;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.MouseEventData;

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

  // interaction events bubbling up to here are those occurring on the overlay layer (excluding holes or children boxes).
  @Override
  public void onMouseDown(InteractiveEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onMouseMove(InteractiveEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onMouseUp(InteractiveEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onMouseScroll(InteractiveEvent<MouseEventData.In> e) {
    e.stopPropagation();
  }

  @Override
  public void onKeyDown(InteractiveEvent<KeyboardEventData.In> e) {
    e.stopPropagation();
  }
}
