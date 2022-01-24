package dev.rebel.chatmate.gui.components;

import javax.annotation.Nonnull;

public abstract class Controller<TControllerProps extends Data<TControllerProps>, TViewProps extends Data<TViewProps>> {
  private TControllerProps prevProps;
  private TViewProps prevViewProps;

  protected final GuiContext context;
  public final ControllerLifeCycle lifeCycle;

  public Controller(GuiContext context) {
    this.context = context;
    this.lifeCycle = new ControllerLifeCycle();
  }

  protected abstract @Nonnull TViewProps onSelectProps(@Nonnull TControllerProps props);

  protected abstract void onDispose();

  private TViewProps selectProps(TControllerProps props) {
    if (this.prevProps == null || !props.compareTo(this.prevProps)) {
      // can't use cached version - recalculate
      this.prevProps = props;
      this.prevViewProps = this.onSelectProps(props);
    }

    return this.prevViewProps;
  }

  private void dispose() {
    this.onDispose();
  }

  public final class ControllerLifeCycle {
    public ControllerLifeCycle() { }

    /** This should be called before each render(). */
    public TViewProps selectProps(TControllerProps props) {
      return Controller.this.selectProps(props);
    }

    /** Call this method when the component is getting removed, after the last render call,
     * if no parent component is rendering it anymore. */
    public void dispose() {
      Controller.this.dispose();
    }
  }
}
