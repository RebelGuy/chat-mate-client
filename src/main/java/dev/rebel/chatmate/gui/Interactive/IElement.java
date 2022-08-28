package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.EventType;
import dev.rebel.chatmate.gui.Interactive.Events.IEvent;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ScreenRenderer;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public interface IElement {
  IElement getParent();
  IElement setParent(IElement parent);
  @Nullable List<IElement> getChildren();

  /** Called automatically at some point before the first render or `calculateThisSize` call. If implementing, ALWAYS
   * call `super.onInitialise()` to prevent null-reference exceptions or similar errors from occurring. */
  void onInitialise();

  // the event data is determined by the event type
  void onEvent(EventType type, IEvent<?> event);

  /** [Upwards] To be called when an element requests the screen to be closed. */
  void onCloseScreen();
  /** [Upwards] To be called when the contents have changed in such a way that a size recalculation is required immediately. will not re-calculate sizes unless this is called somewhere. */
  void onInvalidateSize();
  DimPoint calculateSize(Dim maxWidth); // provides the maximum width that the child's full box can take up, and expects the size of the calculated full box to be returned
  DimPoint getLastCalculatedSize(); // full box size, for caching purposes only - any direct use should probably be avoided
  void setBox(DimRect box); // sets the full box of the element. if the element has any children, it is its responsibility to set the derived boxes of the children too.
  DimRect getBox();
  // render the element to the screen renderer. called every frame. if the element has any children, it is its responsibility to render them too.
  // the element rendering will be performed within the `renderContextWrapper`, if provided. it has to be done this way because rendering is usually deferred, so e.g. changing the GL state before calling `child.render()` won't have an effect.
  void render(@Nullable Consumer<Runnable> renderContextWrapper); // perhaps instead add a transformation object (same concept) that is a matrix (or perhaps a Consumer<Runnable>, which will apply to only visuals), so we can also apply it to the box, and the debug screen?

  boolean getVisible();
  IElement setVisible(boolean visible);

  RectExtension getPadding();
  IElement setPadding(RectExtension padding);

  RectExtension getBorder();
  IElement setBorder(RectExtension border);

  RectExtension getMargin();
  IElement setMargin(RectExtension margin);

  int getZIndex();
  int getEffectiveZIndex();
  IElement setZIndex(int zIndex);

  // how the element should be horizontally positioned in the parent content box. relevant in the parent's setBox method.
  HorizontalAlignment getHorizontalAlignment();
  IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment);

  // how the element should be vertically positioned in the parent content box. relevant in the parent's setBox method.
  VerticalAlignment getVerticalAlignment();
  IElement setVerticalAlignment(VerticalAlignment verticalAlignment);

  SizingMode getSizingMode();
  IElement setSizingMode(SizingMode sizingMode);

  @Nullable String getTooltip();
  IElement setTooltip(@Nullable String text);

  IElement setName(String name);

  /** If set, the element's full width will never exceed this value. */
  IElement setMaxWidth(@Nullable Dim maxWidth);
  /** If set, the element's content width will never exceed this value. */
  IElement setMaxContentWidth(@Nullable Dim maxContentWidth);

  /** If set, attempts to lock the height of the element's full box. No guarantee can be made that the height won't exceed this value. */
  IElement setTargetHeight(@Nullable Dim height);
  @Nullable Dim getTargetHeight();
  /** If set, attempts to lock the height of the element's content box. No guarantee can be made that the height won't exceed this value. */
  IElement setTargetContentHeight(@Nullable Dim contentHeight);
  @Nullable Dim getTargetContentHeight();

  /** Convenience method that automatically casts an element back to its original class type after chaining methods during instantiation. */
  <T extends IElement> T cast();
}
