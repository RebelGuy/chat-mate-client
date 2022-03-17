package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.EventType;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;

import javax.annotation.Nullable;
import java.util.List;

public interface IElement {
  IElement getParent();
  IElement setParent(IElement parent);
  @Nullable List<IElement> getChildren();

  /** Called when the screen is first initialised. */
  void onCreate();

  /** Called when the screen is being removed. */
  void onDispose();


  // the event data is determined by the event type
  void onEvent(EventType type, IEvent<?> event);

  void onCloseScreen(); // to be called when an element requests the screen to be closed
  void onInvalidateSize(); // to be called when the contents have changed in such a way that a size recalculation is required immediately. will not re-calculate sizes unless this is called somewhere.
  DimPoint calculateSize(Dim maxWidth); // provides the maximum width that the child's full box can take up, and expects the size of the calculated full box to be returned
  DimPoint getLastCalculatedSize(); // full box size, for caching purposes only - any direct use should probably be avoided
  void setBox(DimRect box); // sets the full box of the element. if the element has any children, it is its responsibility to set the derived boxes of the children too.
  DimRect getBox();
  void render(); // called every frame. if the element has any children, it is its responsibility to render them too.

  boolean getVisible();
  IElement setVisible(boolean visible);

  RectExtension getPadding();
  IElement setPadding(RectExtension padding);

  RectExtension getMargin();
  IElement setMargin(RectExtension margin);

  int getZIndex();
  IElement setZIndex(int zIndex);

  boolean getFocusable();
  IElement setFocusable(boolean focusable);

  // how the element should be horizontally positioned in the parent content box. relevant in the parent's setBox method.
  HorizontalAlignment getHorizontalAlignment();
  IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment);

  // how the element should be vertically positioned in the parent content box. relevant in the parent's setBox method.
  VerticalAlignment getVerticalAlignment();
  IElement setVerticalAlignment(VerticalAlignment verticalAlignment);

  SizingMode getSizingMode();
  IElement setSizingMode(SizingMode sizingMode);
}
