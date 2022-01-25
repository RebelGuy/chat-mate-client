package dev.rebel.chatmate.gui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// connected instances should be held on to by the parent until they get disposed.
// this means we ourselves will hold on to any child connected instances (whether they are rendered or not).
// i.e. rendering, then hiding, then rendering at button will use the same instance, but the instance will have
// disposed called on it once in the sequence. note that instantiation happens automatically.
public final class Component<
    TControllerProps extends Data<TControllerProps>,
    TViewProps extends Data<TViewProps>,
    TViewState extends Data<TViewState>,
    TController extends Controller<TControllerProps, TViewProps>,
    TView extends View<TViewProps, TViewState>> {
  // for debugging
  private static long COUNTER = 0;
  private final long instanceId;
  private final ComponentManager componentManager;

  private GuiContext context;
  private LazyFactory<TControllerProps, TViewProps, TViewState, TController, TView> lazyFactory;
  private List<Component> allComponents;
  private List<ComponentManager.ReadyComponent> prevComponents;
  private List<ComponentManager.ReadyComponent> components;

  private TViewProps nextViewProps;

  public Component(GuiContext context, LazyFactory<TControllerProps, TViewProps, TViewState, TController, TView> lazyFactory) {
    this.instanceId = COUNTER++;
    this.componentManager = new ComponentManager();
    this.context = context;
    this.lazyFactory = lazyFactory;
    this.prevComponents = new ArrayList<>();
    this.components = new ArrayList<>();
  }

  public void setProps(TControllerProps props) {
    TController controller = this.lazyFactory.getOrCreateController(this.context);
    this.nextViewProps = controller.lifeCycle.selectProps(props);
  }

  public void preRender() {
    if (this.nextViewProps == null) {
      throw new RuntimeException("Cannot render the connected component before calling Component::setProps()");
    }

    TView view = this.lazyFactory.getOrCreateView(this.componentManager);
    view.lifeCycle.updateProps(this.nextViewProps);
  }

  public void render() {
    TView view = this.lazyFactory.getOrCreateView(this.componentManager);
    this.components = view.lifeCycle.render();

    List<Component> newComponents = this.components.stream()
        .flatMap(comp -> comp.getOrderedComponents().stream())
        .filter(comp -> !this.allComponents.contains(comp))
        .collect(Collectors.toList());
    this.allComponents.addAll(newComponents);

    // now we want to render the other components.
    // we don't have to do any special treatment for new components, since
    // everything is handled by the new component's class class :)
    this.components.forEach(Component::setProps); // todo: how to get props set by this view's render method? will need to pass them in here
    this.components.forEach(Component::preRender);
    this.components.forEach(Component::render);
    this.components.forEach(Component::postRender);
  }

  public void postRender() {
    if (this.prevComponents != null) {
      // dispose of any components that are no longer being rendered. This gives them a chance to e.g. unsubscribe from events.
      this.prevComponents.stream().filter(con -> !this.components.contains(con)).forEach(Component::dispose);
    }

    this.prevComponents = this.components;
    this.nextViewProps = null;
  }

  public void dispose() {
    this.components.forEach(Component::dispose);
    this.lazyFactory.dispose();
    this.prevComponents = new ArrayList<>();
    this.components = new ArrayList<>();
    this.nextViewProps = null;

    // do NOT reset this.allComponents - we want to hold on to the instances.
  }

  public static abstract class LazyFactory<
      TControllerProps extends Data<TControllerProps>,
      TViewProps extends Data<TViewProps>,
      TViewState extends Data<TViewState>,
      TController extends Controller<TControllerProps, TViewProps>,
      TView extends View<TViewProps, TViewState>> {

    private TController controller;
    private TView view;

    public final TController getOrCreateController(GuiContext context) {
      if (this.controller == null) {
        this.controller = this.createController(context);
      }
      return this.controller;
    }

    public final TView getOrCreateView(ComponentManager componentManager) {
      if (this.view == null) {
        this.view = this.createView(componentManager);
      }
      return this.view;
    }

    public final void dispose() {
      if (this.controller != null) {
        this.controller.lifeCycle.dispose();
        this.controller = null;
      }
      if (this.view != null) {
        this.view.lifeCycle.dispose();
        this.view = null;
      }
    }

    public abstract TController createController(GuiContext context);
    public abstract TView createView(ComponentManager componentManager);
  }
}


/*
GUI LIFECYCLE

mc.displayGuiScreen(screen)
	- calls prevScreen.onGuiClosed()
	- calls screen.setWorldAndResolution()
		- sets width and height
		- resets buttons
		- calls this.initGui()
mc.resize()
	- calls currentScreen.onResize

runTick(), then does the following operations on the current screen:
	- screen.handleInput
		- handleMouseInput [fires GuiScreenEvent.MouseInputEvent pre (so handleMouseInput can be cancelled) and post]
			- mouseClicked(scaledX, scaledY, mouseButton)
				calls button.mousePressed on all buttons.
				for each handled, selects the button and calls this.actionPerformed(button)
			- mouseReleased(scaledX, scaledY, mouseButton)
				calls selectedButton.mouseReleased
			- mouseClickMove(scaledX, scaledY, mouseButton, timeSinceLastClick)
				[empty]
		- handleKeyboardInput [fires GuiScreenEvent.KeyboardInputEvent pre (so handleKeyboardInput can be cancelled) and post]
			- this.keyTyped(typedChar, eventKey)
				closes GuiScreen if key is ESC
			- broadcasts to mc object (e.g. for getting to fullscreen mode, take screenshots
	- screen.updateScreen
		[empty]


GuiScreen methods to be called externally:
- public drawScreen(mouseX, mouseY, partialTicks)
	- for each button and label, calls the `draw*` functions
- drawHoveringText(lines, x, y)

// for the following, the `component` is used as a dummy to generate the desired behaviour as if the user is interacting with a chat component.
- handleComponentHover(component, x, y)
- handleComponentClick(component)

- drawDefaultBackground/drawWorldBackground
	if in options, draws the `optionsBackground` texture, if in world, draws background with a vertical gradient
- openWebLink
 */