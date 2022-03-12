package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
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

import java.util.function.Function;

public class InteractiveScreen extends Screen implements IElement {
  private IElement mainElement;
  private final InteractiveContext context;
  private final InteractiveListeners listeners;

  private final Function<MouseEventData.In, MouseEventData.Out> onMouseDown = this::_onMouseDown;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseMove = this::_onMouseMove;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseUp = this::_onMouseUp;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseScroll = this::_onMouseScroll;
  private final Function<KeyboardEventData.In, KeyboardEventData.Out> onKeyDown = this::_onKeyDown;

  public InteractiveScreen(InteractiveContext context, InteractiveListeners listeners) {
    super();

    this.context = context;
    this.listeners = listeners;

    context.mouseEventService.on(MouseEventService.Events.MOUSE_DOWN, this.onMouseDown, new MouseEventData.Options(true), this);
    context.mouseEventService.on(MouseEventService.Events.MOUSE_MOVE, this.onMouseMove, new MouseEventData.Options(true), this);
    context.mouseEventService.on(MouseEventService.Events.MOUSE_UP, this.onMouseUp, new MouseEventData.Options(true), this);
    context.mouseEventService.on(MouseEventService.Events.MOUSE_SCROLL, this.onMouseScroll, new MouseEventData.Options(true), this);
    context.keyboardEventService.on(KeyboardEventService.Events.KEY_DOWN, this.onKeyDown, new KeyboardEventData.Options(true, null, null, null), this);
  }

  public void setMainElement(IElement mainElement) {
    this.mainElement = mainElement;
  }

  @Override
  public void onInvalidateSize() {
    this.mainElement.calculateSize(this.context.dimFactory.getMinecraftSize().getX());
  }

  @Override
  public void initGui() {
    if (this.mainElement == null) {
      throw new RuntimeException("MainElement has not been set yet.");
    }

    this.mainElement.onCreate();

    // initial size calculations - required so that things like mouse events can be sent to the correct elements
    this.mainElement.calculateSize(this.context.dimFactory.getMinecraftSize().getX());
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (this.mainElement == null) {
      throw new RuntimeException("MainElement has not been set yet.");
    }

    // events have fired, so recalculate sizes and then render
    this.mainElement.calculateSize(this.context.dimFactory.getMinecraftSize().getX());
    this.mainElement.render();
  }

  @Override
  public void onGuiClosed() {
    this.mainElement.onDispose();
    this.listeners.onClosed.run();
  }

  // note: the mouse location does not need to be translated for the root element, because it is assumed to be positioned
  // at 0,0
  private MouseEventData.Out _onMouseDown(MouseEventData.In in) {
    boolean handled = this.mainElement.onMouseDown(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseMove(MouseEventData.In in) {
    boolean handled = this.mainElement.onMouseMove(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseUp(MouseEventData.In in) {
    boolean handled = this.mainElement.onMouseUp(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private MouseEventData.Out _onMouseScroll(MouseEventData.In in) {
    boolean handled = this.mainElement.onMouseScroll(in);
    return new MouseEventData.Out(handled ? MouseHandlerAction.HANDLED : null);
  }

  private KeyboardEventData.Out _onKeyDown(KeyboardEventData.In in) {
    boolean handled = this.mainElement.onKeyDown(in);
    return new KeyboardEventData.Out(handled ? KeyboardHandlerAction.HANDLED : null);
  }

  //region Empty or delegated IElement methods
  @Override
  public void onCreate() { this.mainElement.onCreate(); }

  @Override
  public void onDispose() { this.mainElement.onDispose(); }

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
  public void setVisible(boolean visible) {
    this.mainElement.setVisible(visible);
  }

  @Override
  public RectExtension getPadding() {
    return new RectExtension(this.context.dimFactory.zeroGui());
  }

  @Override
  public void setPadding(RectExtension padding) { }

  @Override
  public RectExtension getMargin() {
    return new RectExtension(this.context.dimFactory.zeroGui());
  }

  @Override
  public void setMargin(RectExtension margin) { }
  //endregion

  public static class InteractiveContext {
    public final MouseEventService mouseEventService;
    public final KeyboardEventService keyboardEventService;
    public final DimFactory dimFactory;
    private final Minecraft minecraft;
    private final FontRenderer fontRenderer;

    public InteractiveContext(MouseEventService mouseEventService, KeyboardEventService keyboardEventService, DimFactory dimFactory, Minecraft minecraft, FontRenderer fontRenderer) {
      this.mouseEventService = mouseEventService;
      this.keyboardEventService = keyboardEventService;
      this.dimFactory = dimFactory;
      this.minecraft = minecraft;
      this.fontRenderer = fontRenderer;
    }
  }

  public static class InteractiveListeners {
    public final Runnable onClosed;

    public InteractiveListeners(Runnable onClosed) {
      this.onClosed = onClosed;
    }
  }
}
