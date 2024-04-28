package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent.EventType;
import dev.rebel.chatmate.gui.Interactive.Layout.*;
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
  boolean isInitialised();

  // the event data is determined by the event type
  void onEvent(EventType type, InteractiveEvent<?> event);

  /** [Upwards] To be called when an element requests the screen to be closed. */
  void onCloseScreen();
  void onDisposed();
  /** [Upwards] To be called when the contents have changed in such a way that a size recalculation is required immediately. will not re-calculate sizes unless this is called somewhere.
   * Careful: the layout will be regenerated before a render until no more elements invalidate their sizes, so it is possible to run into an infinite loop. */
  void onInvalidateSize();
  DimPoint calculateSize(Dim maxWidth); // provides the maximum width that the child's full box can take up, and expects the size of the calculated full box to be returned
  DimPoint getLastCalculatedSize(); // full box size, for caching purposes only - any direct use should probably be avoided
  void setBox(DimRect box); // sets the full box of the element. if the element has any children, it is its responsibility to set the derived boxes of the children too.
  DimRect getBox();
  // render the element to the screen renderer. called every frame. if the element has any children, it is its responsibility to render them too.
  // the element rendering (and ONLY the element rendering) will be performed within the `renderContextWrapper`, if provided. it has to be done this way because rendering is usually deferred, so e.g. changing the GL state before calling `child.render()` won't have an effect.
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

  int getDepth();

  // if defined, any and all visual content of this element, and all its children, will be clipped if exceeding this box
  @Nullable DimRect getVisibleBox();
  IElement setVisibleBox(@Nullable DimRect visibleBox);

  // how the element should be horizontally positioned in the parent content box. relevant in the parent's setBox method.
  HorizontalAlignment getHorizontalAlignment();
  IElement setHorizontalAlignment(HorizontalAlignment horizontalAlignment);

  // how the element should be vertically positioned in the parent content box. relevant in the parent's setBox method.
  VerticalAlignment getVerticalAlignment();
  IElement setVerticalAlignment(VerticalAlignment verticalAlignment);

  SizingMode getSizingMode();
  IElement setSizingMode(SizingMode sizingMode);

  LayoutGroup getLayoutGroup();
  IElement setLayoutGroup(LayoutGroup layoutGroup);

  @Nullable String getTooltip();
  IElement setTooltip(@Nullable String text);

  IElement setOnClick(@Nullable Runnable onClick);

  IElement setName(String name);
  String getName();

  /** If set, the element's full width will never exceed this value. */
  IElement setMaxWidth(@Nullable Dim maxWidth);
  /** If set, the element's content width will never exceed this value. */
  IElement setMaxContentWidth(@Nullable Dim maxContentWidth);

  /** If set (full width), signifies that the element is willing to shrink down up to this size. Not setting this implies that the element wants to take up as much space in the parent as possible. */
  IElement setMinWidth(@Nullable Dim minWith);
  /** Full width. */
  @Nullable Dim getMinWidth();

  /** If set, attempts to lock the height of the element's full box. No guarantee can be made that the height won't exceed this value. */
  IElement setTargetHeight(@Nullable Dim height);
  @Nullable Dim getTargetHeight();
  /** Takes into account this and the ancestors' target height. */
  @Nullable Dim getEffectiveTargetHeight();
  /** If set, attempts to lock the height of the element's content box. No guarantee can be made that the height won't exceed this value. */
  IElement setTargetContentHeight(@Nullable Dim contentHeight);
  @Nullable Dim getTargetContentHeight();

  /** Convenience method that automatically casts an element back to its original class type after chaining methods during instantiation. */
  <T extends IElement> T cast();
}
