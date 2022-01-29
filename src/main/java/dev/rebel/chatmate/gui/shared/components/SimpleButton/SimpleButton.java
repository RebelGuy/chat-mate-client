package dev.rebel.chatmate.gui.shared.components.SimpleButton;

import dev.rebel.chatmate.gui.components.*;
import dev.rebel.chatmate.services.events.models.GuiScreenMouse;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleButton extends Component.ComponentFactory {
  @Override
  public @Nonnull Controller createController(GuiContext context) {
    return new SimpleButtonController(context);
  }

  @Override
  public @Nonnull View createView(ComponentManager componentManager) {
    return new SimpleButtonView(componentManager);
  }

  public static class Props extends ComponentData.ControllerPropsWithChildren<Props> {
    public Runnable onClick;

    public Props(Runnable onClick) {
      this.onClick = onClick;
    }

    @Override
    public Props copy() {
      return new Props(this.onClick);
    }

    @Override
    public boolean compareTo(Props other) {
      return false;
    }
  }

  public static class VProps extends ComponentData.ViewPropsWithChildren<VProps> {
    public Consumer<Function<GuiScreenMouse.In, GuiScreenMouse.Out>> mouseHandler;
    public Runnable onClick;

    public VProps(Consumer<Function<GuiScreenMouse.In, GuiScreenMouse.Out>> mouseHandler, Runnable onClick) {
      this.mouseHandler = mouseHandler;
      this.onClick = onClick;
    }

    @Override
    public VProps copy() {
      return new VProps(this.mouseHandler, this.onClick);
    }

    @Override
    public boolean compareTo(VProps other) {
      return false;
    }
  }

  public static class State extends ComponentData.ViewState<State> {
    boolean isHovering;

    public State(boolean isHovering) {
      this.isHovering = false;
    }

    @Override
    public State copy() {
      return new State(this.isHovering);
    }

    @Override
    public boolean compareTo(State other) {
      return false;
    }
  }
}
