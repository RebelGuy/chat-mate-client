package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;

public interface IElement {
  /** Called when the screen is first initialised. */
  void onCreate();

  /** Called when the screen is being removed. */
  void onDispose();

  boolean onMouseDown(MouseEventData.In in);
  boolean onMouseMove(MouseEventData.In in);
  boolean onMouseUp(MouseEventData.In in);
  boolean onMouseScroll(MouseEventData.In in);
  boolean onKeyDown(KeyboardEventData.In in);

  void onInvalidateSize(); // to be called when the contents have changed in such a way that a size recalculation is required immediately.
  DimPoint calculateSize(Dim maxWidth); // includes the full box
  void setBox(DimRect box);
  DimRect getBox();
  void render();

  boolean getVisible();
  void setVisible(boolean visible);

  RectExtension getPadding();
  void setPadding(RectExtension padding);

  RectExtension getMargin();
  void setMargin(RectExtension margin);
}
