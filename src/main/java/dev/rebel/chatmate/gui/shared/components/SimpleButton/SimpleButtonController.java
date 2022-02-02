package dev.rebel.chatmate.gui.shared.components.SimpleButton;

import dev.rebel.chatmate.gui.components.Controller;
import dev.rebel.chatmate.gui.dashboard.DashboardScreen;
import dev.rebel.chatmate.gui.shared.components.SimpleButton.SimpleButton.*;
import dev.rebel.chatmate.gui.components.GuiContext;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.MouseEventService.Events;
import dev.rebel.chatmate.services.events.models.InputEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.events.models.MouseEventData.Options;

import javax.annotation.Nonnull;

public class SimpleButtonController extends Controller<GuiContext, Props, VProps> {
  private final MouseEventService mouseEventService;

  public SimpleButtonController(GuiContext context) {
    super(context);
    this.mouseEventService = context.mouseEventService;
  }

  @Override
  protected @Nonnull
  VProps onSelectProps(@Nonnull Props props) {
    return new VProps(handler -> mouseEventService.on(Events.MOUSE_DOWN, handler, new Options(false, MouseButton.LEFT_BUTTON), this), props.onClick);
  }

  @Override
  protected void onDispose() {

  }
}
