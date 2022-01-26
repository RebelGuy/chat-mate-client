package dev.rebel.chatmate.gui.dashboard.components.DashboardMainView;

import dev.rebel.chatmate.gui.components.*;
import dev.rebel.chatmate.gui.dashboard.DashboardContext;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainView.*;

import javax.annotation.Nonnull;

public class DashboardMainView extends Component.ComponentFactory<DashboardContext, Props, VProps, State, DashboardMainViewController, DashboardMainViewView> {
  @Override
  public @Nonnull DashboardMainViewController createController(DashboardContext context) {
    return new DashboardMainViewController(context);
  }

  @Override
  public @Nonnull DashboardMainViewView createView(ComponentManager<DashboardContext> componentManager) {
    return new DashboardMainViewView(componentManager);
  }

  public static class Props extends ComponentData.ControllerProps<Props> {
    private final int width;
    private final int height;
    private final Runnable onCloseScreen;

    public Props(int width, int height, Runnable onCloseScreen) {
      this.width = width;
      this.height = height;
      this.onCloseScreen = onCloseScreen;
    }

    @Override
    public Props copy() {
      return new Props(this.width, this.height, this.onCloseScreen);
    }

    @Override
    public boolean compareTo(Props other) {
      return this.width == other.width && this.height == other.height;
    }
  }

  public static class VProps extends ComponentData.ViewProps<VProps> {
    public VProps() {

    }

    @Override
    public VProps copy() {
      return new VProps();
    }

    @Override
    public boolean compareTo(VProps other) {
      return false;
    }
  }

  public static class State extends ComponentData.ViewState<State> {
    public State() {

    }

    @Override
    public State copy() {
      return new State();
    }

    @Override
    public boolean compareTo(State other) {
      return false;
    }
  }
}
