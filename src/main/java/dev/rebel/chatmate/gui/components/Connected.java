package dev.rebel.chatmate.gui.components;

import java.util.ArrayList;
import java.util.List;

public final class Connected<
    TControllerProps extends Data<TControllerProps>,
    TViewProps extends Data<TViewProps>,
    TViewState extends Data<TViewState>,
    TController extends Controller<TControllerProps, TViewProps>,
    TView extends View<TViewProps, TViewState>> {
  // for debugging
  private static long COUNTER = 0;
  private final long instanceId;

  private GuiContext context;
  private LazyFactory<TControllerProps, TViewProps, TViewState, TController, TView> lazyFactory;
  private List<Connected> prevConnected;
  private List<Connected> connected;

  private TViewProps nextViewProps;

  public Connected(GuiContext context, LazyFactory<TControllerProps, TViewProps, TViewState, TController, TView> lazyFactory) {
    this.instanceId = COUNTER++;
    this.context = context;
    this.lazyFactory = lazyFactory;
    this.prevConnected = new ArrayList<>();
    this.connected = new ArrayList<>();
  }

  public void setProps(TControllerProps props) {
    TController controller = this.lazyFactory.getOrCreateController(this.context);
    this.nextViewProps = controller.lifeCycle.selectProps(props);
  }

  public void preRender() {
    TView view = this.lazyFactory.getOrCreateView();
    view.lifeCycle.updateProps(this.nextViewProps);
  }

  public void render() {
    TView view = this.lazyFactory.getOrCreateView();
    this.connected = view.lifeCycle.render();

    // now we want to render the other components.
    // we don't have to do any special treatment for new components, since
    // everything is handled by the new component's Connected class :)
    this.connected.forEach(Connected::setProps); // todo: how to get props set by this view's render method? will need to pass them in here
    this.connected.forEach(Connected::preRender);
    this.connected.forEach(Connected::render);
    this.connected.forEach(Connected::postRender);
  }

  public void postRender() {
    if (this.prevConnected != null) {
      // dispose of any components that are no longer being rendered
      this.prevConnected.stream().filter(con -> !this.connected.contains(con)).forEach(Connected::dispose);
    }

    this.prevConnected = this.connected;
  }

  public void dispose() {
    this.lazyFactory.getOrCreateController(this.context).lifeCycle.dispose();
    this.lazyFactory.getOrCreateView().lifeCycle.dispose();
    this.prevConnected = null;
    this.connected = null;
    this.context = null;
    this.lazyFactory = null;
    this.nextViewProps = null;
  }

  public static abstract class LazyFactory<
      TControllerProps extends Data<TControllerProps>,
      TViewProps extends Data<TViewProps>,
      TViewState extends Data<TViewState>,
      TController extends Controller<TControllerProps, TViewProps>,
      TView extends View<TViewProps, TViewState>> {

    public abstract TController getOrCreateController(GuiContext context);
    public abstract TView getOrCreateView();
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