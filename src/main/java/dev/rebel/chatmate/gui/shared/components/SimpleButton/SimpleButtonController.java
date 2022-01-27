package dev.rebel.chatmate.gui.shared.components.SimpleButton;

import dev.rebel.chatmate.gui.components.Controller;
import dev.rebel.chatmate.gui.dashboard.DashboardScreen;
import dev.rebel.chatmate.gui.shared.components.SimpleButton.SimpleButton.*;
import dev.rebel.chatmate.gui.components.GuiContext;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.models.GuiScreenMouse;

import javax.annotation.Nonnull;

public class SimpleButtonController extends Controller<GuiContext, Props, VProps> {
  private final ForgeEventService forgeEventService;

  public SimpleButtonController(GuiContext context) {
    super(context);
    this.forgeEventService = context.forgeEventService;
  }

  @Override
  protected @Nonnull
  VProps onSelectProps(@Nonnull Props props) {
    return new VProps(handler -> forgeEventService.onGuiScreenMouse(this, handler, new GuiScreenMouse.Options(DashboardScreen.class)), props.onClick);
  }

  @Override
  protected void onDispose() {

  }
}
