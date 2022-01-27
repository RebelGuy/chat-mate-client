package dev.rebel.chatmate.gui.components;

import javax.annotation.Nonnull;

public abstract class Controller<
    TContext extends GuiContext,
    TControllerProps extends ComponentData.ControllerProps<TControllerProps>,
    TViewProps extends ComponentData.ViewProps<TViewProps>> {
  // for debugging
  private static long COUNTER = 0;
  private final long instanceId;

  private TControllerProps prevProps;
  private TViewProps prevViewProps;

  protected final TContext context;
  public final ControllerLifeCycle lifeCycle;

  public Controller(TContext context) {
    this.instanceId = COUNTER++;
    this.context = context;
    this.lifeCycle = new ControllerLifeCycle();
  }

  protected abstract @Nonnull TViewProps onSelectProps(@Nonnull TControllerProps props);

  protected abstract void onDispose();

  private TViewProps selectProps(TControllerProps props) {
    // todo: this caching is too aggressive - we also have to re-calculate view props if the context changes.
    // perhaps we will have to make controllers "subscribe" to stores, and if a store changes between renders we know we can't use caching?
    // also stores will need to provide a getState() function that returns a data object.
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

  /** Wrapper class for triggering the Controller's's lifecycle methods. ONLY call methods within this class. */
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
