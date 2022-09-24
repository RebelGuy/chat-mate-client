package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.GuiChatMateHud;
import dev.rebel.chatmate.gui.GuiChatMateHudScreen;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore.IHudStoreListener;
import dev.rebel.chatmate.gui.Interactive.ElementHelpers;
import dev.rebel.chatmate.gui.Interactive.Events;
import dev.rebel.chatmate.gui.Interactive.Events.EventPhase;
import dev.rebel.chatmate.gui.Interactive.Events.EventType;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.ContextMenuService;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.Out.MouseHandlerAction;
import dev.rebel.chatmate.services.events.models.Tick;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dev.rebel.chatmate.services.util.Objects.castedVoid;

// note: use the `ChatMateHudStore` to add components to this screen.
public class ChatMateHudScreen extends InteractiveScreen implements IHudStoreListener {
  private final ContextMenuService contextMenuService;
  private final Config config;
  private final GuiChatMateHud legacyHudOverlay;

  // unlike the new HUD screen, the legacy one has to be re-instantiated every time the HUD menu is opened
  private final Supplier<GuiChatMateHudScreen> legacyHudScreenFactory;
  private @Nullable GuiChatMateHudScreen legacyHudScreen;
  private boolean shown;

  public ChatMateHudScreen(ChatMateHudStore chatMateHudStore, ContextMenuService contextMenuService, InteractiveContext context, Config config, GuiChatMateHud guiChatMateHud) {
    super(context, null, InteractiveScreenType.HUD);
    this.contextMenuService = contextMenuService;
    this.config = config;
    this.legacyHudOverlay = guiChatMateHud;
    this.legacyHudScreenFactory = () -> new GuiChatMateHudScreen(context.minecraft, context.mouseEventService, context.dimFactory, guiChatMateHud);
    this.legacyHudScreen = null;

    super.setMainElement(new MainContainer(chatMateHudStore, this.context, this));

    // don't have to worry about unsubscribing because this Screen instance is re-used during the entirety of the application lifetime
    this.context.forgeEventService.onRenderTick(this::onRenderTick, null);
    config.getHudEnabledEmitter().onChange(this::onChangeHudEnabled);
    chatMateHudStore.addListener(this);

    this.onChangeHudEnabled(config.getHudEnabledEmitter().get());
  }

  public void show() {
    this.shown = true;
    this.initialise();
    this.legacyHudScreen = this.legacyHudScreenFactory.get();
  }

  public void hide() {
    this.shown = false;
    super.context.renderer.runSideEffect(() -> {
      this.cleanUp();
    });
    this.legacyHudScreen = null;
  }

  public void initialise() {
    // super.initGui is called only when the Screen is shown
    super.recalculateLayout();
  }

  @Override
  public void onAddElement(HudElement element) {
    castedVoid(MainContainer.class, this.mainElement, el -> el.addElement(element));
  }

  @Override
  public void onRemoveElement(HudElement element) {
    castedVoid(MainContainer.class, this.mainElement, el -> el.removeElement(element));
  }

  @Override
  public void onSetVisible(boolean visible) {
    if (!visible) {
      this.show();
    } else {
      this.hide();
    }
  }

  @Override
  public void onGuiClosed() {
    // called automatically when the Screen was removed from Minecraft - clean up should have already been done
    if (this.shouldCloseScreen) {
      // throw, else we would get stuck in an infinite loop
      throw new RuntimeException("`shouldCloseScreen` should be false after the Screen has been closed.");
    }

    this.cleanUp();
  }

  @Override
  protected void doCloseScreen() {
    this.cleanUp();
    super.doCloseScreen();
  }

  /**
   * Cleans up the interactive state.
   */
  private void cleanUp() {
    // fire the MOUSE_EXIT event one last time
    MouseEventData.In in = this.context.mouseEventService.constructSyntheticMoveEvent();
    List<IElement> elements = ElementHelpers.getElementsAtPointInverted(this, in.mousePositionData.point);
    for (IElement element : elements) {
      element.onEvent(EventType.MOUSE_EXIT, new InteractiveEvent<>(EventPhase.TARGET, in, element));
    }

    super.setFocussedElement(null, Events.FocusReason.AUTO);
    super.elementsUnderCursor = new ArrayList<>();
    super.blockingElement = null;
    super.blockedElementsUnderCursor = new ArrayList<>();
    super.debugModeEnabled = false;
    super.debugElementSelected = false;
    super.refreshTimestamp = 0;
    super.shouldCloseScreen = false;

    super.context.debugElement = null;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    // let the screen always be rendered when we render the overlay
  }

  @Override
  protected MouseEventData.Out onMouseDown(MouseEventData.In in) {
    if (this.isInteractivityDisabled()) {
      return new MouseEventData.Out();
    }

    MouseEventData.Out out = super.onMouseDown(in);
    if (out.handlerAction == null && in.mouseButtonData.eventButton == MouseButton.RIGHT_BUTTON) {
      this.contextMenuService.showHudContext(in.mousePositionData.x, in.mousePositionData.y);
      return new MouseEventData.Out(MouseHandlerAction.HANDLED);
    }

    return new MouseEventData.Out();
  }

  @Override
  protected MouseEventData.Out onMouseMove(MouseEventData.In in) {
    if (this.isInteractivityDisabled()) {
      return new MouseEventData.Out();
    }

    return super.onMouseMove(in);
  }

  @Override
  protected MouseEventData.Out onMouseUp(MouseEventData.In in) {
    if (this.isInteractivityDisabled()) {
      return new MouseEventData.Out();
    }

    return super.onMouseUp(in);
  }

  @Override
  protected MouseEventData.Out onMouseScroll(MouseEventData.In in) {
    if (this.isInteractivityDisabled()) {
      return new MouseEventData.Out();
    }

    return super.onMouseScroll(in);
  }

  @Override
  protected KeyboardEventData.Out onKeyDown(KeyboardEventData.In in) {
    if (this.isInteractivityDisabled()) {
      return new KeyboardEventData.Out();
    }

    return super.onKeyDown(in);
  }

  private Tick.Out onRenderTick(Tick.In event) {
    // render the legacy HUD
    if (this.config.getChatMateEnabledEmitter().get() && this.config.getHudEnabledEmitter().get() && !super.context.minecraft.gameSettings.showDebugInfo) {
      if (this.legacyHudScreen != null) {
        this.legacyHudScreen.renderGameOverlayPreHud();
      }
      this.legacyHudOverlay.renderGameOverlay();
      if (this.legacyHudScreen != null) {
        this.legacyHudScreen.renderGameOverlayPostHud();
      }
    }

    if (this.shown) {
      super.drawScreen(0, 0, 0);
    }
    return new Tick.Out();
  }

  private boolean isInteractivityDisabled() {
    return super.context.minecraft.currentScreen != this || !this.shown;
  }

  private void onChangeHudEnabled(Boolean enabled) {
    if (enabled) {
      this.show();
    } else {
      this.hide();
    }
  }
}
