package dev.rebel.chatmate.gui.components;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// connected instances should be held on to by the parent until they get disposed.
// this means we ourselves will hold on to any child connected instances (whether they are rendered or not).
// i.e. rendering, then hiding, then rendering at button will use the same instance, but the instance will have
// disposed called on it once in the sequence. note that instantiation happens automatically.

// to aid in readability and maintainability, generic type parameters are usually omitted except in public-facing object creation APIs.
// this should be enough to ensure, at compile time, that all required type relationships are respected.
public final class Component<
    TContext extends GuiContext,
    TControllerProps extends ComponentData.ControllerProps<TControllerProps>,
    TViewProps extends ComponentData.ViewProps<TViewProps>,
    TViewState extends ComponentData.ViewState<TViewState>,
    TController extends Controller<TContext, TControllerProps, TViewProps>,
    TView extends View<TViewProps, TViewState>> {
  // for debugging
  private static long COUNTER = 0;
  private final long instanceId;
  private final TContext context;
  private final ComponentManager<TContext> componentManager;

  private ComponentFactory<TContext, TControllerProps, TViewProps, TViewState, TController, TView> componentFactory;
  private List<Component> allComponents;
  private List<ReadyComponent> prevComponents;
  private List<ReadyComponent> components;

  private TViewProps nextViewProps;

  public Component(TContext context, ComponentFactory<TContext, TControllerProps, TViewProps, TViewState, TController, TView> componentFactory) {
    this.instanceId = COUNTER++;
    this.componentManager = new ComponentManager(context);
    this.context = context;
    this.componentFactory = componentFactory;
    this.prevComponents = new ArrayList<>();
    this.components = new ArrayList<>();
    this.allComponents = new ArrayList<>();
  }

  public void setProps(TControllerProps props) {
    TController controller = this.componentFactory.getOrCreateController(this.context);
    this.nextViewProps = controller.lifeCycle.selectProps(props);
  }

  public void preRender() {
    if (this.nextViewProps == null) {
      throw new RuntimeException("Cannot render the connected component before calling Component::setProps() at least once");
    }

    TView view = this.componentFactory.getOrCreateView(this.componentManager);
    view.lifeCycle.updateProps(this.nextViewProps);
  }

  public void render() {
    TView view = this.componentFactory.getOrCreateView(this.componentManager);
    this.components = view.lifeCycle.render();

    List<Component> newComponents = extractComponents(this.components)
        .filter(comp -> !this.allComponents.contains(comp))
        .collect(Collectors.toList());
    this.allComponents.addAll(newComponents);

    // now we want to render the other components.
    // we don't have to do any special treatment for new components, since
    // everything is handled by the new component's class :)
    this.components.forEach(comp -> comp.component.setProps(comp.props));
    extractComponents(this.components).forEach(Component::preRender);
    extractComponents(this.components).forEach(Component::render);
    extractComponents(this.components).forEach(Component::postRender);
  }

  public void postRender() {
    if (this.prevComponents != null) {
      // dispose of any components that are no longer being rendered. This gives them a chance to e.g. unsubscribe from events.
      Stream<Component> prevComponents = extractComponents(this.prevComponents);
      Stream<Component> currentComponents = extractComponents(this.components);
      prevComponents.filter(c -> !currentComponents.collect(Collectors.toList()).contains(c))
          .forEach(Component::dispose);
    }

    this.prevComponents = this.components;
  }

  public void dispose() {
    extractComponents(this.components).forEach(Component::dispose);
    this.componentFactory.dispose();
    this.prevComponents = new ArrayList<>();
    this.components = new ArrayList<>();
    this.nextViewProps = null;

    // do NOT reset this.allComponents - we want to hold on to the instances.
  }

  private static Stream<Component> extractComponents(List<ReadyComponent> readyComponents) {
    return readyComponents.stream().map(comp -> comp.component);
  }

  public static abstract class ComponentFactory<
      TContext extends GuiContext,
      TControllerProps extends ComponentData.ControllerProps<TControllerProps>,
      TViewProps extends ComponentData.ViewProps<TViewProps>,
      TViewState extends ComponentData.ViewState<TViewState>,
      TController extends Controller<TContext, TControllerProps, TViewProps>,
      TView extends View<TViewProps, TViewState>> {

    private TController controller;
    private TView view;

    public final TController getOrCreateController(TContext context) {
      if (this.controller == null) {
        this.controller = this.createController(context);
      }
      return this.controller;
    }

    public final TView getOrCreateView(ComponentManager<TContext> componentManager) {
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

    public abstract @Nonnull TController createController(TContext context);
    public abstract @Nonnull TView createView(ComponentManager<TContext> componentManager);
  }

  public static class StaticComponent<Props extends ComponentData.ControllerProps<?>> implements IChild {
    public final String id;
    public final Props nextProps;
    public final Class<? extends ComponentFactory> factory;

    public StaticComponent(String id, Props nextProps, Class<? extends ComponentFactory> factory) {
      this.id = id;
      this.nextProps = nextProps;
      this.factory = factory;
    }
  }

  public static class ReadyComponent {
    public final Component component;
    public final ComponentData.ControllerProps props;

    public ReadyComponent(Component component, ComponentData.ControllerProps props) {
      this.component = component;
      this.props = props;
    }
  }

  public static class Children implements IChild {
    public Component.StaticComponent[] childrenArray = new StaticComponent[0];

    public Children(IChild... children) {
      List<Component.StaticComponent> childrenList = new ArrayList<>();

      for (IChild child : children) {
        if (child instanceof Children) {
          // this occurs when a View adds its provided children to the children of a component it renders
          childrenList.addAll(Arrays.asList(((Children)child).childrenArray));
        } else if (child instanceof StaticComponent) {
          // this occurs when a View defines a new individual child to a component it renders
          childrenList.add((StaticComponent)child);
        } else {
          throw new RuntimeException("Cannot add an IChild of type " + child.getClass().getSimpleName());
        }
      }
    }
  }

  // This interface is only here to improve the typings of adding existing children to another component's children when rendering components in a View.
  public interface IChild { }
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




How mouse events should be handled:
- Mouse events are created in the event service, similar to how it's already done. Make sure to also include the exact (scaled, but using float) positions.
  - Honestly, we should be dealing with deltas here at all - just report the current position and type of event
- Mouse and keyboard events should return a "handled" and "consumed" property - if handled, simple switch a boolean so other components know. if swallowed, stop immediately.
- Let's say we have a ButtonComponent:
  - A property of the GuiContext.Input should be a onMouseDown, onMouseUp, etc (or something similar)
  - One of the controller's props is onClick
  - The Controller passes the GuiContext.Input class and onClick callback to the View
  - The View, on initialisation, hooks up the onMouseDown to a local function
  - In this function, if the mouse is clicked, checks coords
  - If the button is clicked, returns this.props.onClick
  - Remember that the view, onDispose, must unsubscribe from the event
- Similar story for keyboard events
- We can create a MouseSequence component that uses internal state to track the mouse event sequence, e.g. starting point and ending point of a drag, etc.


 */