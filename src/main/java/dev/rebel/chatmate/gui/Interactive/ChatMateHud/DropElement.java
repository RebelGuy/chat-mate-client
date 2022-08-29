package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.Interactive.Events;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.SingleElement;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;

import javax.annotation.Nullable;
import java.util.List;

public class DropElement extends SingleElement {
  private final IDropElementListener listener;

  public DropElement(InteractiveScreen.InteractiveContext context, IElement parent, IDropElementListener listener) {
    super(context, parent);
    this.listener = listener;
  }

  @Override
  public @Nullable List<IElement> getChildren() {
    return null;
  }

  @Override
  public DimPoint calculateThisSize(Dim maxContentSize) {
    // claim to have no size (so it doesn't interfere with the layout), and set the custom box below
    return new DimPoint(ZERO, ZERO);
  }

  @Override
  public void setBox(DimRect box) {
    // observe the whole screen. in the future we could get a `getRect` function if we want to allow changing this dynamically
    super.setBox(super.context.dimFactory.getMinecraftRect());
  }

  @Override
  public void onMouseMove(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.pressedButtons.contains(MouseButton.LEFT_BUTTON)) {
      this.listener.onDrag(e.getData().mousePositionData.point);
      e.stopPropagation();
    }
  }

  @Override
  public void onMouseUp(Events.IEvent<MouseEventData.In> e) {
    if (e.getData().mouseButtonData.eventButton == MouseButton.LEFT_BUTTON) {
      this.listener.onDrop(e.getData().mousePositionData.point);
      e.stopPropagation();
    }
  }

  @Override
  public void renderElement() {
    // not visible
  }

  public interface IDropElementListener {
    /** Called when the mouse button was dragged. */
    void onDrag(DimPoint position);
    /** Called when the mouse button was released. */
    void onDrop(DimPoint position);
  }
}
