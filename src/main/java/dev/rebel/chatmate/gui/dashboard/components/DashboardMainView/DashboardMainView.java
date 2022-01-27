package dev.rebel.chatmate.gui.dashboard.components.DashboardMainView;

import dev.rebel.chatmate.gui.components.*;
import dev.rebel.chatmate.gui.dashboard.DashboardContext;
import dev.rebel.chatmate.gui.dashboard.components.DashboardMainView.DashboardMainView.*;
import net.minecraft.client.Minecraft;

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
    public int width;
    public int height;
    public Minecraft minecraft;
    public Runnable onCloseScreen;

    public Props(int width, int height, Minecraft minecraft, Runnable onCloseScreen) {
      this.width = width;
      this.height = height;
      this.minecraft = minecraft;
      this.onCloseScreen = onCloseScreen;
    }

    @Override
    public Props copy() {
      return new Props(this.width, this.height, this.minecraft, this.onCloseScreen);
    }

    @Override
    public boolean compareTo(Props other) {
      return this.width == other.width && this.height == other.height;
    }
  }

  public static class VProps extends ComponentData.ViewProps<VProps> {
    public int width;
    public int height;
    public Minecraft minecraft;
    public Runnable onClick;

    public VProps(int width, int height, Minecraft minecraft, Runnable onCloseScreen) {
      this.width = width;
      this.height = height;
      this.minecraft = minecraft;
      this.onClick = onCloseScreen;
    }

    @Override
    public VProps copy() {
      return new VProps(this.width, this.height, this.minecraft, this.onClick);
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
