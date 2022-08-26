package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Environment;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.Events.*;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.Layout.VerticalAlignment;
import dev.rebel.chatmate.gui.Screen;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.KeyboardEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.KeyboardEventData.Out.KeyboardHandlerAction;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

// note: this is the top-level screen that is responsible for triggering element renders and passing through interactive events.
// it does not fully implement the IElement interface (most things are meaningless) - just enough to glue things together.
// the correct way would have been to split IElement up into two interfaces, but it doesn't really matter.
public class InteractiveScreen extends Screen implements IElement {
  protected InteractiveContext context;
  private final @Nullable GuiScreen parentScreen;

  private final Function<MouseEventData.In, MouseEventData.Out> _onMouseDown = this::onMouseDown;
  private final Function<MouseEventData.In, MouseEventData.Out> _onMouseMove = this::onMouseMove;
  private final Function<MouseEventData.In, MouseEventData.Out> _onMouseUp = this::onMouseUp;
  private final Function<MouseEventData.In, MouseEventData.Out> _onMouseScroll = this::onMouseScroll;
  private final Function<KeyboardEventData.In, KeyboardEventData.Out> _onKeyDown = this::onKeyDown;

  protected boolean requiresRecalculation = true;
  protected boolean shouldCloseScreen = false;

  protected IElement mainElement = null;
  protected List<IElement> elementsUnderCursor = new ArrayList<>();
  protected IElement blockingElement = null;
  protected List<IElement> blockedElementsUnderCursor = new ArrayList<>();  // contains the list of elements that this element blocks, if any
  protected boolean debugModeEnabled = false;
  protected boolean debugElementSelected = false;
  protected long refreshTimestamp; // for showing a quick tooltip after F5 is pressed

  public InteractiveScreen(InteractiveContext context, @Nullable GuiScreen parentScreen) {
    super();

    this.context = context;
    this.parentScreen = parentScreen;
    this.refreshTimestamp = 0;

    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_DOWN, this._onMouseDown, new MouseEventData.Options(true), this);
    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_MOVE, this._onMouseMove, new MouseEventData.Options(true), this);
    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_UP, this._onMouseUp, new MouseEventData.Options(true), this);
    this.context.mouseEventService.on(MouseEventService.Events.MOUSE_SCROLL, this._onMouseScroll, new MouseEventData.Options(true), this);
    this.context.keyboardEventService.on(KeyboardEventService.Events.KEY_DOWN, this._onKeyDown, new KeyboardEventData.Options(true, null, null, null), this);
  }

  public void setMainElement(IElement mainElement) {
    this.mainElement = mainElement;
  }

  @Override
  public void onInvalidateSize() {
    if (this.mainElement == null) {
      return;
    }
    this.requiresRecalculation = true;
  }

  @Override
  public void onCloseScreen() {
    this.shouldCloseScreen = true;
  }

  private void doCloseScreen() {
    // fire the MOUSE_EXIT event one last time
    MouseEventData.In in = this.context.mouseEventService.constructSyntheticMoveEvent();
    List<IElement> elements = ElementHelpers.getElementsAtPointInverted(this, in.mousePositionData.point);
    for (IElement element : elements) {
        element.onEvent(EventType.MOUSE_EXIT, new InteractiveEvent<>(EventPhase.TARGET, in, element));
    }

    this.mc.displayGuiScreen(this.parentScreen);
    if (this.mc.currentScreen == null) {
      this.mc.setIngameFocus();
    }
  }

  @Override
  protected void onScreenSizeUpdated() {
    this.onInvalidateSize();

    SizeData eventData = new SizeData(this.context.dimFactory.getMinecraftSize());
    for (IElement element : ElementHelpers.getAllChildren(this)) {
      element.onEvent(EventType.WINDOW_RESIZE, new InteractiveEvent<>(EventPhase.TARGET, eventData, element));
    }
  }

  @Override
  public void initGui() {
    if (this.mainElement == null) {
      throw new RuntimeException("Please set the MainElement before displaying the screen in Minecraft.");
    }

    // this will force all elements to initialise
    this.recalculateLayout();

    List<InputElement> autoFocusable = Collections.filter(ElementHelpers.getElementsOfType(this.mainElement, InputElement.class), InputElement::getAutoFocus);
    if (Collections.any(autoFocusable)) {
      InputElement toFocus = Collections.min(autoFocusable, InputElement::getTabIndex);
      this.setFocussedElement(toFocus, FocusReason.AUTO);
    }
  }

  // this always fires after any element changes so that, by the time we get to rendering, everything has been laid out
  protected void recalculateLayout() {
    if (this.mainElement == null || this.shouldCloseScreen) {
      return;
    }

    this.context.renderer._executeSideEffects();

    if (!this.requiresRecalculation && this.mainElement.getBox() != null) {
      return;
    }
    this.requiresRecalculation = false;

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
    DimRect mainRect = ElementHelpers.alignElementInBox(mainSize, screenRect, this.mainElement.getHorizontalAlignment(), this.mainElement.getVerticalAlignment());
    mainRect = mainRect.clamp(screenRect);
    this.mainElement.setBox(mainRect);
    this.context.renderer._executeSideEffects();

    // fire a synthetic mouse event since elements that previously depended on the mouse position may have been moved as a side effect
    // it is not clear if we should do this after EVERY side effect execution (if side effects were scheduled), or only after size invalidation - for now, see how we go.
    this.onMouseMove(this.context.mouseEventService.constructSyntheticMoveEvent());
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (this.mainElement == null) {
      throw new RuntimeException("Please set the MainElement before displaying the screen in Minecraft.");
    } else if (this.shouldCloseScreen) {
      this.doCloseScreen();
      return;
    }

    this.recalculateLayout();

    this.context.renderer.clear();
    this.mainElement.render();
    this.context.renderer._executeRender();
    this.context.renderer._executeSideEffects();
    this.renderTooltip();

    if (this.context.debugElement != null) {
      ElementHelpers.renderDebugInfo(this.context.debugElement, this.context);
    }
  }

  @Override
  public void onGuiClosed() {
    // it is very important that we remove the reference to the mainElement, otherwise
    // this screen will never be garbage collected since mainElement holds a reference to it
    this.mainElement = null;
    this.context = null;
    this.elementsUnderCursor = null;
    this.blockingElement = null;
    this.blockedElementsUnderCursor = null;

    // don't wait around - immediately release references so weak collections (such as the CursorService) update right now
    System.gc();
  }

  // note: the mouse location does not need to be translated for the root element, because it is assumed to be positioned
  // at 0,0
  protected MouseEventData.Out onMouseDown(MouseEventData.In in) {
    if (this.mainElement == null || this.shouldCloseScreen) {
      return new MouseEventData.Out(null);
    }

    // if we are debugging and are in "discovery mode", select the element under the cursor
    if (this.debugModeEnabled && !this.debugElementSelected) {
      IElement element = Collections.first(ElementHelpers.raycast(this, in.mousePositionData.point));
      if (element != null) {
        this.debugElementSelected = true;
        this.context.debugElement = element;
        return new MouseEventData.Out(MouseHandlerAction.SWALLOWED);
      }
    }

    this.recalculateLayout();
    boolean handled = this.propagateMouseEvent(EventType.MOUSE_DOWN, in);
    this.recalculateLayout();
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  protected MouseEventData.Out onMouseMove(MouseEventData.In in) {
    if (this.mainElement == null || this.shouldCloseScreen) {
      return new MouseEventData.Out(null);
    }

    this.context.mousePosition = in.mousePositionData.point;

    // if we are debugging and haven't selected an element, enter "discovery mode" by temp-debugging the element under the cursor
    if (this.debugModeEnabled && !this.debugElementSelected) {
      IElement element = Collections.first(ElementHelpers.raycast(this, in.mousePositionData.point));
      if (element != null) {
        this.context.debugElement = element;
        return new MouseEventData.Out(MouseHandlerAction.SWALLOWED);
      }
    }

    // fire MOUSE_ENTER/MOUSE_EXIT events
    // if an element blocks the sequence propagation, then the blocked elements will be treated as if for a MOUSE_EXIT event.
    // they will be added back as soon as the blocking element is no longer included in the new elements.
    List<IElement> newElements = ElementHelpers.getElementsAtPointInverted(this, in.mousePositionData.point);
    List<IElement> newOrExistingEntered = new ArrayList<>();
    List<IElement> previousElements = this.elementsUnderCursor;
    List<IElement> blocked = this.blockingElement != null && newElements.contains(this.blockingElement) ? this.blockedElementsUnderCursor : new ArrayList<>();
    List<IElement> newBlocked = new ArrayList<>();
    IElement newBlocking = null;
    for (IElement newElement : newElements) {
      boolean isBlocked = Collections.any(blocked, el -> el == newElement);
      if (!isBlocked) {
        newOrExistingEntered.add(newElement);
        InteractiveEvent<MouseEventData.In> event = new InteractiveEvent<>(EventPhase.CAPTURE, in, newElement);
        newElement.onEvent(EventType.MOUSE_ENTER, event);

        // go as far as we can or until propagation has stopped
        // all remaining elements are now blocked
        if (event.stoppedPropagation) {
          newBlocked = Collections.filter(newElements, el -> !newOrExistingEntered.contains(el));
          newBlocking = newElement;
          break;
        }
      }
    }
    for (IElement prevElement : previousElements) {
      if (!newOrExistingEntered.contains(prevElement)) {
        // state change from entered to exited
        prevElement.onEvent(EventType.MOUSE_EXIT, new InteractiveEvent<>(EventPhase.TARGET, in, prevElement));
      }
    }
    for (IElement newElement : Collections.reverse(newOrExistingEntered)) { // reverse
      if (!previousElements.contains(newElement)) {
        // state change from exited to entered
        newElement.onEvent(EventType.MOUSE_ENTER, new InteractiveEvent<>(EventPhase.TARGET, in, newElement));
      }
    }

    this.elementsUnderCursor = newOrExistingEntered;
    this.blockedElementsUnderCursor = newBlocked;
    this.blockingElement = newBlocking;

    this.recalculateLayout();
    boolean handled = this.propagateMouseEvent(EventType.MOUSE_MOVE, in);
    this.recalculateLayout();
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  protected MouseEventData.Out onMouseUp(MouseEventData.In in) {
    if (this.mainElement == null || this.shouldCloseScreen) {
      return new MouseEventData.Out(null);
    }

    this.recalculateLayout();
    boolean handled = this.propagateMouseEvent(EventType.MOUSE_UP, in);
    this.recalculateLayout();
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  protected MouseEventData.Out onMouseScroll(MouseEventData.In in) {
    if (this.mainElement == null || this.shouldCloseScreen) {
      return new MouseEventData.Out(null);
    }

    this.recalculateLayout();
    boolean handled = this.propagateMouseEvent(EventType.MOUSE_SCROLL, in);
    this.recalculateLayout();
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  protected KeyboardEventData.Out onKeyDown(KeyboardEventData.In in) {
    if (this.mainElement == null || this.shouldCloseScreen) {
      return new KeyboardEventData.Out(null);
    }

    // first handle debug controls
    if (this.debugModeEnabled && this.debugElementSelected && this.context.debugElement != null) {
      if (in.isPressed(Keyboard.KEY_UP) && this.context.debugElement.getParent() != null) {
        // go to parent
        this.context.debugElement = this.context.debugElement.getParent();
        return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
      } else if (in.isPressed(Keyboard.KEY_DOWN) && Collections.any(Collections.filter(this.context.debugElement.getChildren(), IElement::getVisible))) {
        // go to first child
        this.context.debugElement = Collections.first(Collections.filter(this.context.debugElement.getChildren(), IElement::getVisible));
        return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
      } else if (this.context.debugElement.getParent() != null && (in.isPressed(Keyboard.KEY_LEFT) || in.isPressed(Keyboard.KEY_RIGHT))) {
        List<IElement> siblings = Collections.filter(this.context.debugElement.getParent().getChildren(), IElement::getVisible);
        if (Collections.size(siblings) > 1) {
          int currentIndex = siblings.indexOf(this.context.debugElement);
          int delta = in.isPressed(Keyboard.KEY_LEFT) ? -1 : 1;
          this.context.debugElement = Collections.elementAt(siblings, currentIndex + delta);
          return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
        }
      }
    }

    // now do the normal event propagation
    this.recalculateLayout();
    boolean handled = this.propagateKeyboardEvent(EventType.KEY_DOWN, in);
    this.recalculateLayout();
    if (handled) {
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    }

    // fallback key handling
    if (this.mainElement != null && in.isPressed(Keyboard.KEY_ESCAPE)) {
      this.onCloseScreen();
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    } else if (in.isPressed(Keyboard.KEY_F11)) {
      this.context.minecraft.toggleFullscreen();
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    } else if (in.isPressed(Keyboard.KEY_F3)) {
      this.toggleDebug();
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    } else if (in.isPressed(Keyboard.KEY_F5)) {
      // force a refresh
      this.refreshTimestamp = new Date().getTime();
      this.onInvalidateSize();
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);

    } else if (in.isPressed(Keyboard.KEY_TAB) && this.context.focusedElement != null) {
      // focus onto the next element
      List<InputElement> inputElements = ElementHelpers.getElementsOfType(this.mainElement, InputElement.class);
      List<InputElement> focusable = Collections.filter(inputElements, InputElement::canTabFocus);
      if (Collections.any(focusable)) {
        List<InputElement> sorted = Collections.orderBy(focusable, InputElement::getTabIndex);
        int currentIndex = sorted.indexOf(this.context.focusedElement);
        int delta = in.isKeyModifierActive(KeyboardEventData.In.KeyModifier.SHIFT) ? -1 : 1;
        InputElement newFocus = Collections.elementAt(sorted, currentIndex + delta);
        this.setFocussedElement(newFocus, FocusReason.TAB);
      }
    }

    this.recalculateLayout();
    return new KeyboardEventData.Out(null);
  }

  private void toggleDebug() {
    this.debugModeEnabled = !this.debugModeEnabled;
    this.debugElementSelected = false;
    this.context.debugElement = null;
  }

  private boolean propagateMouseEvent(Events.EventType type, MouseEventData.In data) {
    if (this.mainElement == null || this.shouldCloseScreen) {
      return false;
    }

    // collect the focus while we're at it - if no element along the path accepts a focus, we will unfocus the currently focussed element.
    boolean refocus = type == EventType.MOUSE_DOWN;

    List<IElement> elements = ElementHelpers.getElementsAtPoint(this, data.mousePositionData.point);
    IElement target = Collections.last(elements);
    InteractiveEvent<MouseEventData.In> captureEvent = new InteractiveEvent<>(EventPhase.CAPTURE, data, target);
    InputElement newFocus = null;
    for (IElement element : elements) {
      if (element instanceof InputElement) {
        InputElement maybeNewFocus = (InputElement)element;
        newFocus = maybeNewFocus.canFocus() ? maybeNewFocus : newFocus;
      }

      element.onEvent(type, captureEvent);
      if (captureEvent.stoppedPropagation) {
        break;
      }
    }

    if (refocus) {
      this.setFocussedElement(newFocus, FocusReason.CLICK);
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

  /** Sets the new focussed element and fires appropriate events. */
  protected void setFocussedElement(@Nullable InputElement newFocus, FocusReason reason) {
    InputElement oldFocus = this.context.focusedElement;
    if (oldFocus != newFocus) {
      this.context.focusedElement = newFocus;
      FocusEventData focusData = new FocusEventData(oldFocus, newFocus, reason);
      if (oldFocus != null) {
        InteractiveEvent<FocusEventData> blurEvent = new InteractiveEvent<>(EventPhase.TARGET, focusData, oldFocus);
        oldFocus.onEvent(EventType.BLUR, blurEvent);
      }
      if (newFocus != null) {
        InteractiveEvent<FocusEventData> focusEvent = new InteractiveEvent<>(EventPhase.TARGET, focusData, newFocus);
        newFocus.onEvent(EventType.FOCUS, focusEvent);
      }
    }
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

  /** Starting at the MOUSE_ENTER blocking element and working our way upwards, display the first non-null tooltip. */
  private void renderTooltip() {
    if (this.elementsUnderCursor.size() == 0) {
      return;
    }

    List<IElement> candidates = new ArrayList<>();
    for (IElement element : this.elementsUnderCursor) {
      candidates.add(element);
      if (element == this.blockingElement) {
        break;
      }
    }

    @Nullable String tooltip = null;
    if (new Date().getTime() - this.refreshTimestamp < 2000L) {
      tooltip = "Forced layout refresh";
    } else {
      for (IElement element : Collections.reverse(candidates)) {
        tooltip = element.getTooltip();
        if (tooltip != null) {
          break;
        }
      }
    }

    DimPoint mousePos = context.mousePosition;
    if (tooltip == null || mousePos == null) {
      return;
    }

    RendererHelpers.drawTooltip(context.dimFactory, context.fontEngine, context.mousePosition, tooltip);
  }

  //region Empty or delegated IElement methods
  @Override
  public List<IElement> getChildren() { return Collections.list(this.mainElement); }

  @Override
  public IElement getParent() { return null; }

  @Override
  public IElement setParent(IElement parent) { return null; }

  @Override
  public void onInitialise() { }

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
  public RectExtension getBorder() { return new RectExtension(this.context.dimFactory.zeroGui()); }

  @Override
  public InteractiveScreen setBorder(RectExtension border) { return this; }

  @Override
  public RectExtension getMargin() { return new RectExtension(this.context.dimFactory.zeroGui()); }

  @Override
  public InteractiveScreen setMargin(RectExtension margin) { return this; }

  @Override
  public int getZIndex() { return 0; }

  @Override
  public int getEffectiveZIndex() { return 0; }

  @Override
  public IElement setZIndex(int zIndex) { return this; }

  @Override
  public InteractiveScreen setHorizontalAlignment(HorizontalAlignment horizontalAlignment) { return null; }

  @Override
  public HorizontalAlignment getHorizontalAlignment() { return null; }

  @Override
  public InteractiveScreen setVerticalAlignment(VerticalAlignment verticalAlignment) { return null; }

  @Override
  public VerticalAlignment getVerticalAlignment() { return null;}

  @Override
  public IElement setSizingMode(SizingMode sizingMode) { return null; }

  @Override
  public SizingMode getSizingMode() { return null; }

  @Override
  public <T extends IElement> T cast() { return null; }

  @Override
  public @Nullable String getTooltip() { return null; }

  @Override
  public IElement setTooltip(@Nullable String text) { return null; }

  @Override
  public IElement setName(String name) { return null; }

  @Override
  public IElement setMaxWidth(@Nullable Dim maxWidth) { return null; }

  @Override
  public IElement setMaxContentWidth(@Nullable Dim maxContentWidth) { return null; }

  @Override
  public IElement setTargetHeight(@Nullable Dim height) { return null; }

  @Override
  public IElement setTargetContentHeight(@Nullable Dim height) { return null; }

  @Override
  public @Nullable Dim getTargetHeight() { return null; }

  @Override
  public @Nullable Dim getTargetContentHeight() { return null; }

  //endregion

  public static class InteractiveContext {
    public final ScreenRenderer renderer;
    public final MouseEventService mouseEventService;
    public final KeyboardEventService keyboardEventService;
    public final DimFactory dimFactory;
    public final Minecraft minecraft;
    public final FontEngine fontEngine;
    public final ClipboardService clipboardService;
    public final SoundService soundService;
    public final CursorService cursorService;
    public final MinecraftProxyService minecraftProxyService;
    public final UrlService urlService;
    public final Environment environment;
    public final LogService logService;
    public final MinecraftChatService minecraftChatService;
    public final ForgeEventService forgeEventService;

    /** The element that we want to debug. */
    public @Nullable IElement debugElement = null;
    public @Nullable InputElement focusedElement = null;
    public @Nullable DimPoint mousePosition = null;

    public InteractiveContext(ScreenRenderer renderer,
                              MouseEventService mouseEventService,
                              KeyboardEventService keyboardEventService,
                              DimFactory dimFactory,
                              Minecraft minecraft,
                              FontEngine fontEngine,
                              ClipboardService clipboardService,
                              SoundService soundService,
                              CursorService cursorService,
                              MinecraftProxyService minecraftProxyService,
                              UrlService urlService,
                              Environment environment,
                              LogService logService,
                              MinecraftChatService minecraftChatService,
                              ForgeEventService forgeEventService) {
      this.renderer = renderer;
      this.mouseEventService = mouseEventService;
      this.keyboardEventService = keyboardEventService;
      this.dimFactory = dimFactory;
      this.minecraft = minecraft;
      this.fontEngine = fontEngine;
      this.clipboardService = clipboardService;
      this.soundService = soundService;
      this.cursorService = cursorService;
      this.minecraftProxyService = minecraftProxyService;
      this.urlService = urlService;
      this.environment = environment;
      this.logService = logService;
      this.minecraftChatService = minecraftChatService;
      this.forgeEventService = forgeEventService;
    }
  }

  // This manages the render order of elements to simulate z indexes. For some reason, OpenGL ignores z values when
  // translating or rendering, otherwise we would just use the built-in functionality.
  public static class ScreenRenderer {
    // importantly, renderables within a layer are ordered
    private Map<Integer, List<Runnable>> collectedRenders;
    private Set<Runnable> completedRenders;
    private final List<Runnable> sideEffects;
    private boolean sideEffectsInProgress;

    public ScreenRenderer() {
      this.clear();
      this.sideEffects = java.util.Collections.synchronizedList(new ArrayList<>());
      this.sideEffectsInProgress = false;
    }

    public void clear() {
      this.collectedRenders = new HashMap<>();
      this.completedRenders = new HashSet<>();
    }

    public void render(IElement element, Runnable onRender) {
      int zIndex = element.getEffectiveZIndex();
      if (!this.collectedRenders.containsKey(zIndex)) {
        this.collectedRenders.put(zIndex, new ArrayList<>());
      }
      this.collectedRenders.get(zIndex).add(onRender);
    }

    /** Waits until the current render or layout-calculation cycle is complete before running the specified side effect.
     * May run the side effect immediately. Note that you will need to manually invalidate your size if required. <br/><br/>
     * If you get a crash because a required layout value (such as a box) is null, then that's most likely because you
     * tried to modify an element when you shouldn't have, and you should instead call `runSideEffect`. <br/>
     * In almost all cases, you should use `runSideEffect` when responding to the result of an async request. */
    public void runSideEffect(Runnable sideEffect) {
      synchronized (this.sideEffects) {
        if (this.sideEffectsInProgress) {
          // it is possible that running a side effect causes another side effect to be run - we can safely run it immediately
          sideEffect.run();
        } else {
          this.sideEffects.add(sideEffect);
        }
      }
    }

    public void _executeRender() {
      // we don't have all render methods of all elements initially, so it's not possible to render an element very early
      // that is supposed to be rendered on top of everything else. it works best when siblings have differing z indexes.
      //
      // note that the overall render order is still unchanged, we are merely enhancing it.

      // algorithm: always exhaust the list of lowest z index elements, then move on to the next layer, etc, constantly
      // checking whether there are new lower-layer elements to be rendered (though we wouldn't expect there to be any
      // since the effective z index is additive).
      @Nullable Runnable renderable;
      while (true) {
        renderable = getNextRenderable();
        if (renderable == null) {
          break;
        }
        renderable.run();
        this.completedRenders.add(renderable);
      }

      this.clear();
    }

    public void _executeSideEffects() {
      this.sideEffectsInProgress = true;

      List<Runnable> copy;
      synchronized (this.sideEffects) {
        copy = Collections.list(this.sideEffects);
        this.sideEffects.clear();
      }
      copy.forEach(Runnable::run);

      this.sideEffectsInProgress = false;
    }

    private @Nullable Runnable getNextRenderable() {
      @Nullable Runnable result = null;
      while (result == null) {
        if (this.collectedRenders.size() == 0) {
          return null;
        }

        int zIndex = this.getLowestZIndex();
        List<Runnable> list = Collections.filter(this.collectedRenders.get(zIndex), r -> !this.completedRenders.contains(r));
        if (list.size() <= 1) {
          this.collectedRenders.remove(zIndex);
        }
        if (list.size() == 0) {
          return null;
        }
        result = list.remove(0);
        if (list.size() > 0) {
          this.collectedRenders.put(zIndex, list);
        }
      }

      return result;
    }

    private int getLowestZIndex() {
      return Collections.min(Collections.list(this.collectedRenders.keySet()), i -> i);
    }
  }
}
