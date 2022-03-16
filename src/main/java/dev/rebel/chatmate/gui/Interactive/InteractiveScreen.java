package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Events.*;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.Screen;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.KeyboardEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.Out.KeyboardHandlerAction;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

// note: this is the top-level screen that is responsible for triggering element renders and passing through interactive events.
// it does not fully implement the IElement interface (most things are meaningless) - just enough to glue things together.
// the correct way would have been to split IElement up into two interfaces, but it doesn't really matter.
public class InteractiveScreen extends Screen implements IElement {
  private InteractiveContext context;
  private final @Nullable GuiScreen parentScreen;

  private final Function<MouseEventData.In, MouseEventData.Out> onMouseDown = this::_onMouseDown;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseMove = this::_onMouseMove;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseUp = this::_onMouseUp;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseScroll = this::_onMouseScroll;
  private final Function<KeyboardEventData.In, KeyboardEventData.Out> onKeyDown = this::_onKeyDown;

  private IElement mainElement = null;
  private boolean debugModeEnabled = false;
  private boolean debugElementSelected = false;

  public InteractiveScreen(InteractiveContext context, @Nullable GuiScreen parentScreen) {
    super();

    this.context = context;
    this.parentScreen = parentScreen;

    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_DOWN, this.onMouseDown, new MouseEventData.Options(true), this);
    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_MOVE, this.onMouseMove, new MouseEventData.Options(true), this);
    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_UP, this.onMouseUp, new MouseEventData.Options(true), this);
    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_SCROLL, this.onMouseScroll, new MouseEventData.Options(true), this);
    this.context.keyboardEventService.on(KeyboardEventService.Events.KEY_DOWN, this.onKeyDown, new KeyboardEventData.Options(true, null, null, null), this);
  }

  public void setMainElement(IElement mainElement) {
    this.mainElement = mainElement;
  }

  @Override
  public void onInvalidateSize() {
    if (this.mainElement == null) {
      return;
    }
    this.recalculateLayout();
  }

  @Override
  public void onCloseScreen() {
    this.mc.displayGuiScreen(this.parentScreen);
    if (this.mc.currentScreen == null) {
      this.mc.setIngameFocus();
    }
  }

  @Override
  protected void onScreenSizeUpdated() {
    this.recalculateLayout();
  }

  @Override
  public void initGui() {
    if (this.mainElement == null) {
      throw new RuntimeException("Please set the MainElement before displaying the screen in Minecraft.");
    }

    this.mainElement.onCreate();

    // initial size calculations - required so that things like mouse events can be sent to the correct elements
    this.recalculateLayout();
  }

  // this always fires after any element changes so that, by the time we get to rendering, everything has been laid out
  private void recalculateLayout() {
    DimFactory factory = this.context.dimFactory;
    Dim maxX = factory.getMinecraftSize().getX();
    Dim maxY = factory.getMinecraftSize().getY();

    // inspired by https://limpet.net/mbrubeck/2014/09/17/toy-layout-engine-6-block.html
    // top-down: give the children a width so they can calculate their size and be positioned properly.
    // bottom-up: once sizes and positions have been calculated, the total box will be passed back up
    DimPoint mainSize = this.mainElement.calculateSize(maxX);

    // now that we know our actual dimensions, pass the full rect down and let the children re-position (but they should
    // not do any resizing as that would invalidate the final box).
    DimRect screenRect = new DimRect(factory.zeroGui(), factory.zeroGui(), maxX, maxY);
    DimRect mainRect = ElementBase.alignElementInBox(mainSize, screenRect, this.mainElement.getHorizontalAlignment(), this.mainElement.getVerticalAlignment());
    mainRect = mainRect.clamp(screenRect);
    this.mainElement.setBox(mainRect);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (this.mainElement == null) {
      throw new RuntimeException("Please set the MainElement before displaying the screen in Minecraft.");
    }

    // events have fired, so recalculate sizes and then render
    // this.mainElement.calculateSize(this.context.dimFactory.getMinecraftSize().getX());
    this.mainElement.render();
  }

  @Override
  public void onGuiClosed() {
    if (this.mainElement != null) {
      this.mainElement.onDispose();

      // it is very important that we remove the reference to the mainElement, otherwise
      // this screen will never be garbage collected since mainElement holds a reference to it
      this.mainElement = null;
      this.context = null;
    }
  }

  // note: the mouse location does not need to be translated for the root element, because it is assumed to be positioned
  // at 0,0
  private MouseEventData.Out _onMouseDown(MouseEventData.In in) {
    if (this.mainElement == null) {
      return new MouseEventData.Out(null);
    }

    // if we are debugging and are in "discovery mode", select the element under the cursor
    if (this.debugModeEnabled && !this.debugElementSelected) {
      IElement element = Collections.last(ElementHelpers.getElementsAtPoint(this, in.mousePositionData.point));
      if (element != null && element != this) {
        this.debugElementSelected = true;
        this.context.debugElement = element;
        return new MouseEventData.Out(MouseHandlerAction.SWALLOWED);
      }
    }

    boolean handled = this.propagateMouseEvent(EventType.MOUSE_DOWN, in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseMove(MouseEventData.In in) {
    if (this.mainElement == null) {
      return new MouseEventData.Out(null);
    }

    this.context.mousePosition = in.mousePositionData.point;

    // if we are debugging and haven't selected an element, enter "discovery mode" by temp-debugging the element under the cursor
    if (this.debugModeEnabled && !this.debugElementSelected) {
      IElement element = Collections.last(ElementHelpers.getElementsAtPoint(this, in.mousePositionData.point));
      if (element != null && element != this) {
        this.context.debugElement = element;
        return new MouseEventData.Out(MouseHandlerAction.SWALLOWED);
      }
    }

    boolean handled = this.propagateMouseEvent(EventType.MOUSE_MOVE, in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseUp(MouseEventData.In in) {
    if (this.mainElement == null) {
      return new MouseEventData.Out(null);
    }

    boolean handled = this.propagateMouseEvent(EventType.MOUSE_UP, in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseScroll(MouseEventData.In in) {
    if (this.mainElement == null) {
      return new MouseEventData.Out(null);
    }

    boolean handled = this.propagateMouseEvent(EventType.MOUSE_SCROLL, in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private KeyboardEventData.Out _onKeyDown(KeyboardEventData.In in) {
    if (this.mainElement == null) {
      return new KeyboardEventData.Out(null);
    }

    // now do the normal event propagation
    boolean handled = this.propagateKeyboardEvent(EventType.KEY_DOWN, in);
    if (handled) {
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    }

    // fallback key handling
    if (this.mainElement != null && in.isPressed(Keyboard.KEY_ESCAPE)) {
      this.onCloseScreen();
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    } else if (in.isPressed(Keyboard.KEY_F3)) {
      this.toggleDebug();
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    }

    return new KeyboardEventData.Out(null);
  }

  private void toggleDebug() {
    this.debugModeEnabled = !this.debugModeEnabled;
    this.debugElementSelected = false;
    this.context.debugElement = null;
  }

  private boolean propagateMouseEvent(Events.EventType type, MouseEventData.In data) {
    if (this.mainElement == null) {
      return false;
    }

    // collect the focus while we're at it - if no element along the path accepts a focus, we will unfocus the currently focussed element.
    boolean refocus = type == EventType.MOUSE_DOWN;

    List<IElement> elements = ElementHelpers.getElementsAtPoint(this, data.mousePositionData.point);
    IElement target = Collections.last(elements);
    InteractiveEvent<MouseEventData.In> captureEvent = new InteractiveEvent<>(EventPhase.CAPTURE, data, target);
    IElement newFocus = null;
    for (IElement element : elements) {
      if (element.getFocusable()) {
        newFocus = element;
      }

      element.onEvent(type, captureEvent);
      if (captureEvent.stoppedPropagation) {
        break;
      }
    }

    IElement oldFocus = context.focusedElement;
    if (refocus && oldFocus != newFocus) {
      context.focusedElement = newFocus;
      FocusEventData focusData = new FocusEventData(oldFocus, newFocus);
      if (oldFocus != null) {
        InteractiveEvent<FocusEventData> blurEvent = new InteractiveEvent<>(EventPhase.TARGET, focusData, oldFocus);
        oldFocus.onEvent(EventType.BLUR, blurEvent);
      }
      if (newFocus != null) {
        InteractiveEvent<FocusEventData> focusEvent = new InteractiveEvent<>(EventPhase.TARGET, focusData, newFocus);
        newFocus.onEvent(EventType.FOCUS, focusEvent);
      }
    }

    if (captureEvent.stoppedPropagation) {
      return true;
    }

    InteractiveEvent<MouseEventData.In> bubbleEvent = new InteractiveEvent<>(EventPhase.BUBBLE, data, target);
    for (IElement element : Collections.reverse(elements)) {
      element.onEvent(type, bubbleEvent);
      if (bubbleEvent.stoppedPropagation) {
        break;
      }
    }

    return bubbleEvent.stoppedPropagation;
  }

  /** Propagates the keyboard event to the currently focused element. */
  private boolean propagateKeyboardEvent(EventType type, KeyboardEventData.In data) {
    IElement target = this.context.focusedElement;
    if (this.mainElement == null || target == null) {
      return false;
    }

    List<IElement> elements = ElementHelpers.findElementFromChild(target, this);
    if (elements == null) {
      System.out.println("Cannot find elements to propagate keyboard event");
      return false;
    }

    InteractiveEvent<KeyboardEventData.In> captureEvent = new InteractiveEvent<>(EventPhase.CAPTURE, data, target);
    for (IElement element : Collections.reverse(elements)) {
      element.onEvent(type, captureEvent);
      if (captureEvent.stoppedPropagation) {
        break;
      }
    }

    if (captureEvent.stoppedPropagation) {
      return true;
    }

    InteractiveEvent<KeyboardEventData.In> bubbleEvent = new InteractiveEvent<>(EventPhase.BUBBLE, data, target);
    for (IElement element : elements) {
      element.onEvent(type, bubbleEvent);
      if (bubbleEvent.stoppedPropagation) {
        break;
      }
    }

    return bubbleEvent.stoppedPropagation;
  }

  //region Empty or delegated IElement methods
  @Override
  public List<IElement> getChildren() { return Collections.list(this.mainElement); }

  @Override
  public IElement getParent() { return null; }

  @Override
  public void onCreate() { }

  @Override
  public void onDispose() { }

  @Override
  public void onEvent(EventType type, IEvent<?> event) { }

  @Override
  public DimPoint calculateSize(Dim maxWidth) { return null; }

  @Override
  public DimPoint getLastCalculatedSize() { return null; }

  @Override
  public DimRect getBox() {
    DimPoint pos = new DimPoint(this.context.dimFactory.zeroGui(), this.context.dimFactory.zeroGui());
    return new DimRect(pos, this.context.dimFactory.getMinecraftSize());
  }

  @Override
  public void setBox(DimRect box) { }

  @Override
  public void render() { }

  @Override
  public boolean getVisible() { return true; }

  @Override
  public InteractiveScreen setVisible(boolean visible) {
    this.mainElement.setVisible(visible);
    return this;
  }

  @Override
  public RectExtension getPadding() { return new RectExtension(this.context.dimFactory.zeroGui()); }

  @Override
  public InteractiveScreen setPadding(RectExtension padding) { return this; }

  @Override
  public RectExtension getMargin() { return new RectExtension(this.context.dimFactory.zeroGui()); }

  @Override
  public InteractiveScreen setMargin(RectExtension margin) { return this; }

  @Override
  public int getZIndex() { return 0; }

  @Override
  public IElement setZIndex(int zIndex) { return this; }

  @Override
  public boolean getFocusable() { return false; }

  @Override
  public IElement setFocusable(boolean focusable) { return null; }

  @Override
  public InteractiveScreen setHorizontalAlignment(HorizontalAlignment horizontalAlignment) { return null; }

  @Override
  public HorizontalAlignment getHorizontalAlignment() { return null; }

  @Override
  public InteractiveScreen setVerticalAlignment(VerticalAlignment verticalAlignment) { return null; }

  @Override
  public VerticalAlignment getVerticalAlignment() { return null;}

  //endregion

  public static class InteractiveContext {
    public final MouseEventService mouseEventService;
    public final KeyboardEventService keyboardEventService;
    public final DimFactory dimFactory;
    public final Minecraft minecraft;
    public final FontRenderer fontRenderer;

    /** The element that we want to debug. */
    public @Nullable IElement debugElement = null;
    public @Nullable IElement focusedElement = null;
    public @Nullable DimPoint mousePosition = null;

    public InteractiveContext(MouseEventService mouseEventService, KeyboardEventService keyboardEventService, DimFactory dimFactory, Minecraft minecraft, FontRenderer fontRenderer) {
      this.mouseEventService = mouseEventService;
      this.keyboardEventService = keyboardEventService;
      this.dimFactory = dimFactory;
      this.minecraft = minecraft;
      this.fontRenderer = fontRenderer;
    }
  }
}
