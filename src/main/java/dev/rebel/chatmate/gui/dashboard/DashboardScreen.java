package dev.rebel.chatmate.gui.dashboard;

import dev.rebel.chatmate.gui.Screen;
import dev.rebel.chatmate.gui.components.Component;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainView;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainView.*;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainViewController;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainViewView;

public class DashboardScreen extends Screen {
  private final DashboardContext context;

  private Component<DashboardContext, Props, VProps, State, DashboardMainViewController, DashboardMainViewView> dashboardComponent;

  public DashboardScreen(DashboardContext context) {
    this.context = context;
  }

  @Override
  public void initGui() {
    if (this.dashboardComponent != null) {
      this.dashboardComponent.dispose();
    }

    this.dashboardComponent = new Component(this.context, new DashboardMainView());
    this.dashboardComponent.setProps(new Props(this.width, this.height, this::closeScreen));
  }

  @Override
  protected void onScreenSizeUpdated() {
    this.dashboardComponent.setProps(new Props(this.width, this.height, this::closeScreen));
  }

  @Override
  public void updateScreen() {
    this.dashboardComponent.preRender();
    this.dashboardComponent.render();
    this.dashboardComponent.postRender();
  }

  @Override
  public void onGuiClosed() {
    this.dashboardComponent.dispose();
  }
}
