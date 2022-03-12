package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In;

/** A single element with no children. */
public abstract class SingleElement extends ElementBase {
  protected boolean visible;

  public SingleElement(InteractiveContext context, IElement parent) {
    super(context, parent);

    this.visible = true;
  }

  @Override
  public abstract DimPoint calculateSize(Dim maxWidth);

  @Override
  public abstract void render();

  @Override
  public boolean getVisible() {
    return this.visible;
  }

  @Override
  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
