package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.events.models.KeyboardEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;

public class ChatMateHudScreen extends InteractiveScreen {
  private boolean shown;

  public ChatMateHudScreen(InteractiveContext context, Config config) {
    super(context, null);

    super.setMainElement(new ButtonElement(context, this).setChildElement(new LabelElement(context, this).setText("This is a test button.")));

    // don't have to worry about unsubscribing because this Screen instance is re-used during the entirety of the application lifetime
    context.forgeEventService.onRenderGameOverlay(this::onRenderOverlay, new RenderGameOverlay.Options(RenderGameOverlayEvent.ElementType.ALL));
    config.getHudEnabledEmitter().onChange(this::onChangeHudEnabled);

    this.onChangeHudEnabled(config.getHudEnabledEmitter().get());
  }

  public void show() {
    this.shown = true;
    this.initialise();
  }

  public void hide() {
    this.shown = false;
  }

  public void initialise() {
    // super.initGui is called only when the Screen is shown
    super.recalculateLayout();
  }

  @Override
  public void onGuiClosed() {
    // called automatically when the Screen was removed from Minecraft - do some cleaning up of interactivity
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

    return super.onMouseDown(in);
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

  private RenderGameOverlay.Out onRenderOverlay(RenderGameOverlay.In in) {
    if (this.shown) {
      super.drawScreen(0, 0, 0);
    }
    return new RenderGameOverlay.Out();
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
