package dev.rebel.chatmate.gui.components;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class View<TProps extends Data<TProps>, TState extends Data<TState>> {
  /** Null only for the first render. */
  private TProps prevProps;
  /** Never null. */
  private TProps props;
  private @Nonnull TState state;
  private @Nullable TState nextState = null;
  /** Never null. */
  private List<Connected> prevOutput;
  private boolean renderInProgress = false;
  private boolean propsUpdated = false;

  public final @Nonnull ViewLifeCycle lifeCycle;

  protected View(@Nonnull TState initialState) {
    this.lifeCycle = new ViewLifeCycle();
    this.state = initialState.copy();
  }

  /** Returns the current state. */
  protected final @Nonnull TState getState() { return this.state.copy(); }

  /** Sets the state to be used the next time render() is called. **Note that setting the state multiple times will overwrite previous calls**. */
  protected final void setState(@Nonnull TState state) {
    if (this.renderInProgress) {
      throw new RuntimeException("Cannot set the state during rendering.");
    }
    this.nextState = state.copy();
  }

  protected final @Nonnull TProps getProps() { return this.props.copy(); }

  // note: the following protected methods are deliberately abstract so there is never confusion about when/whether to call super().

  /** Called before the first render() call. */
  protected abstract void onInitialise(@Nonnull TProps initialProps);

  protected abstract void onUpdate(@Nonnull TProps prevProps, @Nonnull TProps props);

  protected abstract void onDispose();

  // todo: add api so props can be passed into the connected component
  // how to deal with dependencies? perhaps pass in a factory with only a create(controllerProps) interface.
  // but we don't want to instantiate a new instance every single time - how can we deal with that?
  // maybe some kind of "initial render" flag, and instantiate only if true, otherwise use existing instance based on key.
  // this should work because children can only exist if the parent exists.

  // if this list does not contain a component that was previously contained, the component should be disposed.
  // if the list contains a new connected compoennt, instantiate it. yes that's a good idea.
  // the only thing is - how would we compare the connected components? some kind of key?
  protected abstract List<Connected> onRenderComponents();

  protected abstract void onRenderScreen(); // always called

  //////////////////
  //// PRIVATE /////
  //////////////////

  private void updateProps(@Nonnull TProps props) {
    if (this.propsUpdated) {
      throw new RuntimeException("Cannot update props multiple times before calling the render method.");
    }

    this.prevProps = this.props;
    this.props = props;

    if (this.prevProps == null) {
      this.onInitialise(props);
    } else {
      this.onUpdate(this.prevProps, this.props);
    }
  }

  private void dispose() {
    this.onDispose();
  }

  private @Nonnull List<Connected> render() {
    boolean needsRender = this.prevProps == null // initial render
        || this.nextState != null && !this.nextState.compareTo(this.state) // state changed
        || this.propsUpdated && !this.props.compareTo(this.prevProps); // props changed

    renderStart();
    List<Connected> result = needsRender ? this.onRenderComponents() : this.prevOutput;
    this.onRenderScreen();
    this.renderEnd();

    this.prevOutput = result;
    return result == null ? new ArrayList<>() : result;
  }

  private void renderStart() {
    this.renderInProgress = true;

    // shift back the state
    this.state = this.nextState == null ? this.state : this.nextState;
    this.nextState = null;
  }

  private void renderEnd() {
    this.renderInProgress = false;
    this.propsUpdated = false;
  }

  public final class ViewLifeCycle {
    public ViewLifeCycle() { }

    /** This should be called before each render(). */
    public void updateProps(@Nonnull TProps props) {
      View.this.updateProps(props);
    }

    /** Call this method when the component is getting removed, after the last render call,
     * if no parent component is rendering it anymore. */
    public void dispose() {
      View.this.dispose();
    }

    public @Nonnull List<Connected> render() {
      return View.this.render();
    }
  }
}
