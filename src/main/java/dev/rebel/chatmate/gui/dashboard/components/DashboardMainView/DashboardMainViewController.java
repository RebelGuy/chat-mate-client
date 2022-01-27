package dev.rebel.chatmate.gui.dashboard.components.DashboardMainView;

import dev.rebel.chatmate.gui.components.Controller;
import dev.rebel.chatmate.gui.dashboard.DashboardContext;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainView.*;

import javax.annotation.Nonnull;

public class DashboardMainViewController extends Controller<DashboardContext, Props, VProps> {
  public DashboardMainViewController(DashboardContext context) {
    super(context);
  }

  @Nonnull
  @Override
  protected VProps onSelectProps(@Nonnull Props props) {
    return new VProps(props.width, props.height, props.minecraft, props.onCloseScreen);
  }

  @Override
  protected void onDispose() {

  }
}
