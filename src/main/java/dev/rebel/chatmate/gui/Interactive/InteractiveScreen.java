package dev.rebel.chatmate.gui.Interactive;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.function.Function;

// note: this is the top-level screen that is responsible for triggering element renders and passing through interactive events.
// it does not fully implement the IElement interface (most things are meaningless) - just enough to glue things together.
// the correct way would have been to split IElement up into two interfaces, but it doesn't really matter.
public class InteractiveScreen extends Screen implements IElement {
  private final InteractiveContext context;
  private final @Nullable GuiScreen parentScreen;

  private final Function<MouseEventData.In, MouseEventData.Out> onMouseDown = this::_onMouseDown;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseMove = this::_onMouseMove;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseUp = this::_onMouseUp;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseScroll = this::_onMouseScroll;
  private final Function<KeyboardEventData.In, KeyboardEventData.Out> onKeyDown = this::_onKeyDown;

  private IElement mainElement;

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
    }
  }

  // note: the mouse location does not need to be translated for the root element, because it is assumed to be positioned
  // at 0,0
  private MouseEventData.Out _onMouseDown(MouseEventData.In in) {
    boolean handled = this.mainElement != null && this.mainElement.onMouseDown(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseMove(MouseEventData.In in) {
    boolean handled = this.mainElement != null && this.mainElement.onMouseMove(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseUp(MouseEventData.In in) {
    boolean handled = this.mainElement != null && this.mainElement.onMouseUp(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseScroll(MouseEventData.In in) {
    boolean handled = this.mainElement != null && this.mainElement.onMouseScroll(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private KeyboardEventData.Out _onKeyDown(KeyboardEventData.In in) {
    if (this.mainElement != null && in.isPressed(Keyboard.KEY_ESCAPE)) {
      this.mc.displayGuiScreen(this.parentScreen);
      if (this.mc.currentScreen == null) {
        this.mc.setIngameFocus();
      }
      return new KeyboardEventData.Out(KeyboardHandlerAction.SWALLOWED);
    }

    boolean handled = this.mainElement != null && this.mainElement.onKeyDown(in);
    return new KeyboardEventData.Out(handled ? KeyboardHandlerAction.HANDLED : null);
  }

  //region Empty or delegated IElement methods
  @Override
  public void onCreate() { }

  @Override
  public void onDispose() { }

  @Override
  public boolean onMouseDown(MouseEventData.In in) {
    return false;
  }

  @Override
  public boolean onMouseMove(MouseEventData.In in) {
    return false;
  }

  @Override
  public boolean onMouseUp(MouseEventData.In in) {
    return false;
  }

  @Override
  public boolean onMouseScroll(MouseEventData.In in) {
    return false;
  }

  @Override
  public boolean onKeyDown(KeyboardEventData.In in) {
    return false;
  }

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
  public boolean getVisible() {
    return this.mainElement.getVisible();
  }

  @Override
  public InteractiveScreen setVisible(boolean visible) {
    this.mainElement.setVisible(visible);
    return this;
  }

  @Override
  public RectExtension getPadding() {
    return new RectExtension(this.context.dimFactory.zeroGui());
  }

  @Override
  public InteractiveScreen setPadding(RectExtension padding) { return this; }

  @Override
  public RectExtension getMargin() {
    return new RectExtension(this.context.dimFactory.zeroGui());
  }

  @Override
  public InteractiveScreen setMargin(RectExtension margin) { return this; }

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

    public InteractiveContext(MouseEventService mouseEventService, KeyboardEventService keyboardEventService, DimFactory dimFactory, Minecraft minecraft, FontRenderer fontRenderer) {
      this.mouseEventService = mouseEventService;
      this.keyboardEventService = keyboardEventService;
      this.dimFactory = dimFactory;
      this.minecraft = minecraft;
      this.fontRenderer = fontRenderer;
    }
  }
}
