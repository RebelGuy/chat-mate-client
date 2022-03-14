package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;

import javax.annotation.Nullable;
import java.util.List;

public interface IElement {
  IElement getParent();
  @Nullable List<IElement> getChildren();

  /** Called when the screen is first initialised. */
  void onCreate();

  /** Called when the screen is being removed. */
  void onDispose();

  boolean onMouseDown(MouseEventData.In in);
  boolean onMouseMove(MouseEventData.In in);
  boolean onMouseUp(MouseEventData.In in);
  boolean onMouseScroll(MouseEventData.In in);
  boolean onKeyDown(KeyboardEventData.In in);

  void onInvalidateSize(); // to be called when the contents have changed in such a way that a size recalculation is required immediately. will not re-calculate sizes unless this is called somewhere.
  DimPoint calculateSize(Dim maxWidth); // this gives the maximum width that the child's full box can take up
  DimPoint getLastCalculatedSize(); // for caching purposes only - should be used with caution
  void setBox(DimRect box);
  DimRect getBox();
  void render();

  boolean getVisible();
  IElement setVisible(boolean visible);

  RectExtension getPadding();
  IElement setPadding(RectExtension padding);

  RectExtension getMargin();
  IElement setMargin(RectExtension margin);

  int getZIndex();
  IElement setZIndex(int zIndex);

  // how the element should be horizontally positioned in the parent content box. relevant in the parent's setBox method.
  HorizontalAlignment getHorizontalAlignment();
  IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment);

  // how the element should be vertically positioned in the parent content box. relevant in the parent's setBox method.
  VerticalAlignment getVerticalAlignment();
  IElement setVerticalAlignment(VerticalAlignment verticalAlignment);
}
