package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.models.MouseEventData.MouseButtonData.MouseButton;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore.IHudStoreListener;
import dev.rebel.chatmate.gui.Interactive.ElementHelpers;
import dev.rebel.chatmate.gui.Interactive.Events.FocusEventData.FocusReason;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent.EventPhase;
import dev.rebel.chatmate.gui.Interactive.Events.InteractiveEvent.EventType;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.ContextMenuService;
import dev.rebel.chatmate.events.models.KeyboardEventData;
import dev.rebel.chatmate.events.models.MouseEventData;

import java.util.ArrayList;
import java.util.List;

import static dev.rebel.chatmate.util.Objects.castedVoid;

// note: use the `ChatMateHudStore` to add components to this screen.
public class ChatMateHudScreen extends InteractiveScreen implements IHudStoreListener {
  private final ChatMateHudStore chatMateHudStore;
  private final ContextMenuService contextMenuService;
  private final Config config;

  public ChatMateHudScreen(ChatMateHudStore chatMateHudStore, ContextMenuService contextMenuService, InteractiveContext context, Config config) {
    super(context, null, InteractiveScreenType.HUD, LifecycleType.PRIVATE);
    this.chatMateHudStore = chatMateHudStore;
    this.contextMenuService = contextMenuService;
    this.config = config;

    super.setMainElement(new MainContainer(chatMateHudStore, this.context, this));

    // don't have to worry about unsubscribing because this Screen instance is re-used during the entirety of the application lifetime
    this.context.forgeEventService.onRenderTick(this::onRenderTick);
    config.getHudEnabledEmitter().onChange(x -> this.updateVisibility());
    config.getChatMateEnabledEmitter().onChange(x -> this.updateVisibility());
    chatMateHudStore.addListener(this);

    this.updateVisibility();
  }

  public void show() {
    this.initialise();
  }

  public void hide() {
    super.context.renderer.runSideEffect(() -> {
      this.cleanUp();
    });
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
    if (visible) {
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
    MouseEventData data = this.context.mouseEventService.constructSyntheticMoveEvent();
    List<IElement> elements = ElementHelpers.getElementsAtPointInverted(this, data.mousePositionData.point);
    for (IElement element : elements) {
      element.onEvent(EventType.MOUSE_EXIT, new InteractiveEvent<>(EventPhase.TARGET, data, element));
    }

    super.setFocussedElement(null, FocusReason.AUTO);
    super.elementsUnderCursor = new ArrayList<>();
    super.blockingElement = null;
    super.blockedElementsUnderCursor = new ArrayList<>();
    super.debugModeEnabled = false;
    super.debugElementSelected = false;
    super.refreshTimestamp = 0;
    super.shouldCloseScreen = false;

    super.context.debugElement = null;

    this.chatMateHudStore.clearSelectedElements();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    // let the screen always be rendered when we render the overlay
  }

  @Override
  protected void onMouseDown(Event<MouseEventData> event) {
    if (this.isInteractivityDisabled()) {
      return;
    }

    super.onMouseDown(event);

    MouseEventData data = event.getData();
    if (!event.stoppedPropagation && data.mouseButtonData.eventButton == MouseButton.RIGHT_BUTTON) {
      this.contextMenuService.showHudContext(data.mousePositionData.x, data.mousePositionData.y);
      event.stopPropagation();
    }
  }

  @Override
  protected void onMouseMove(Event<MouseEventData> event) {
    if (this.isInteractivityDisabled()) {
      return;
    }

    super.onMouseMove(event);
  }

  @Override
  protected void onMouseUp(Event<MouseEventData> event) {
    if (this.isInteractivityDisabled()) {
      return;
    }

    super.onMouseUp(event);
  }

  @Override
  protected void onMouseScroll(Event<MouseEventData> event) {
    if (this.isInteractivityDisabled()) {
      return;
    }

    super.onMouseScroll(event);
  }

  @Override
  protected void onKeyDown(Event<KeyboardEventData> event) {
    if (this.isInteractivityDisabled()) {
      return;
    }

    super.onKeyDown(event);
  }

  private void onRenderTick(Event<?> event) {
    if (!super.context.minecraft.gameSettings.showDebugInfo || super.shouldCloseScreen) {
      super.drawScreen(0, 0, 0);
    }
  }

  private boolean isInteractivityDisabled() {
    return super.context.minecraft.currentScreen != this;
  }

  private void updateVisibility() {
    boolean visible = this.config.getChatMateEnabledEmitter().get() && this.config.getHudEnabledEmitter().get();
    this.onSetVisible(visible);
  }
}
