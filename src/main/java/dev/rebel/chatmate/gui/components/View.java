package dev.rebel.chatmate.gui.components;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class View<TProps extends ComponentData.ViewProps<TProps>, TState extends ComponentData.ViewState<TState>> {
  // for debugging
  private static long COUNTER = 0;
  private final long instanceId;

  private final @Nonnull ComponentManager componentManager;
  /** Null only for the first render. */
  private TProps prevProps;
  /** Never null. */
  private TProps props;
  private @Nonnull TState state;
  private @Nullable TState nextState = null;
  /** Never null. */
  private List<Component.ReadyComponent> prevOutput;
  private List<Component.ReadyComponent> output;
  private boolean renderInProgress = false;
  private boolean allowAddingComponents = false;
  private boolean propsUpdated = false;

  public final @Nonnull ViewLifeCycle lifeCycle;

  protected View(@Nonnull ComponentManager componentManager, @Nonnull TState initialState) {
    this.instanceId = COUNTER++;
    this.componentManager = componentManager;
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

  protected final void add(Component.StaticComponent component) {
    if (!this.allowAddingComponents) {
      throw  new RuntimeException("Components can only be added within onRenderComponents.");
    }
    Component.ReadyComponent instance = this.componentManager.getOrCreate(component);
    this.output.add(instance);
  }

  /** It is the View's responsibility to render its children (or pass them down to another component), if applicable, otherwise they will get lost. */
  protected final void add(Component.Children children) {
    for (Component.StaticComponent child : children.childrenArray) {
      this.add(child);
    }
  }

  // note: the following protected methods are deliberately abstract so there is never confusion about when/whether to call super().

  /** Called before the first render() call. */
  protected abstract void onInitialise(@Nonnull TProps initialProps);

  protected abstract void onUpdate(@Nonnull TProps prevProps, @Nonnull TProps props);

  protected abstract void onDispose();

  // it is important we call this before rendering children so we have a chance to draw a background.
  // note that rendering components is a way of **deferring** rendering which, ultimately, is ALWAYS done in onRenderScreen() (if we go deep enough).
  /** Always called BEFORE rendering any components. */
  protected abstract void onRenderScreen();

  // todo: add api so props can be passed into the connected component
  // how to deal with dependencies? perhaps pass in a factory with only a create(controllerProps) interface.
  // but we don't want to instantiate a new instance every single time - how can we deal with that?
  // maybe some kind of "initial render" flag, and instantiate only if true, otherwise use existing instance based on key.
  // this should work because children can only exist if the parent exists.

  // todo: the issue right now is that we need to specify Connected components, with the props, and
  // allow them to be instantiated if they are not already, as well as refer to the same reference over several render cycles.
  // it's possible that, as well as the ControllerProps, we might also have to pass in a key - probably a string (name) for readability.
  // this means the instance will be cached by the key. if, after render, they key is not present, but an instance exists, the instance should be `disposed`.
  // might need a big static class of all mappings to get it to work properly.

  // todo: are render props possible? the prop of the controller is a function that, given an input, returns an output.

  // todo: regarding positioning, can use the Transfor object we are inheriting from to either use a relative coordinate system, or an absolute one relative to the screen coords.
  // will also need to handle how to deal with overflow (drawing out of bounds of the parent). option should be either 'hide' or 'overflow' [or, for an advanced challenge, 'scroll']
  protected abstract void onRenderComponents(); // use the

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

  private @Nonnull List<Component.ReadyComponent> render() {
    boolean needsRender = this.prevProps == null // initial render
        || this.nextState != null && !this.nextState.compareTo(this.state) // state changed
        || this.propsUpdated && !this.props.compareTo(this.prevProps); // props changed

    renderStart();
    this.onRenderScreen();
    if (needsRender) {
      this.allowAddingComponents = true;
      this.onRenderComponents();
      this.allowAddingComponents = false;
    }
    List<Component.ReadyComponent> result = needsRender ? this.output : this.prevOutput;
    this.renderEnd();

    this.prevOutput = result;
    return result == null ? new ArrayList<>() : result;
  }

  private void renderStart() {
    this.renderInProgress = true;
    this.output = new ArrayList<>();

    // shift back the state
    this.state = this.nextState == null ? this.state : this.nextState;
    this.nextState = null;
  }

  private void renderEnd() {
    this.output = null;
    this.renderInProgress = false;
    this.propsUpdated = false;
  }

  /** Wrapper class for triggering the View's lifecycle methods. ONLY call methods within this class. */
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

    public @Nonnull List<Component.ReadyComponent> render() {
      return View.this.render();
    }
  }
}
