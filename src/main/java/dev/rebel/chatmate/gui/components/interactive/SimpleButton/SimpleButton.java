package dev.rebel.chatmate.gui.components.interactive.SimpleButton;

import dev.rebel.chatmate.gui.components.*;

public class SimpleButton extends Component.ComponentFactory {
  @Override
  public Controller createController(GuiContext context) {
    return new SimpleButtonController(context);
  }

  @Override
  public View createView(ComponentManager componentManager) {
    return new SimpleButtonView(componentManager);
  }

  public static class Props extends ComponentData.ControllerProps<Props> {
    public Props() {

    }

    @Override
    public Props copy() {
      return new Props();
    }

    @Override
    public boolean compareTo(Props other) {
      return false;
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
