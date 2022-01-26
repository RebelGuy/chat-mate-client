package dev.rebel.chatmate.gui.shared.components.SimpleButton;

import dev.rebel.chatmate.gui.components.Controller;
import dev.rebel.chatmate.gui.shared.components.SimpleButton.SimpleButton.*;
import dev.rebel.chatmate.gui.components.GuiContext;

import javax.annotation.Nonnull;

public class SimpleButtonController extends Controller<GuiContext, Props, VProps> {
  public SimpleButtonController(GuiContext context) {
    super(context);
  }

  @Override
  protected @Nonnull
  VProps onSelectProps(@Nonnull Props props) {
    return new VProps();
  }

  @Override
  protected void onDispose() {

  }
}
