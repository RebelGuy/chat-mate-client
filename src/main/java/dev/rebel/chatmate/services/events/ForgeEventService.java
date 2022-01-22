package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.services.events.models.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

// why? because I would like to keep Forge event subscriptions centralised for an easier overview and for easier debugging.
// it also makes testing specific events A LOT easier because we can just mock out this class and test the event handler
// in complete isolation.
// thank Java for the verbose typings.
public class ForgeEventService {
  private final Minecraft minecraft;

  private final ArrayList<EventHandler<OpenGui.In, OpenGui.Out, OpenGui.Options>> openGuiModListHandlers;
  private final ArrayList<EventHandler<OpenGui.In, OpenGui.Out, OpenGui.Options>> openGuiIngameMenuHandlers;
  private final ArrayList<EventHandler<RenderGameOverlay.In, RenderGameOverlay.Out, RenderGameOverlay.Options>> renderGameOverlayHandlers;
  private final ArrayList<EventHandler<RenderChatGameOverlay.In, RenderChatGameOverlay.Out, RenderChatGameOverlay.Options>> renderChatGameOverlayHandlers;
  private final ArrayList<EventHandler<Tick.In, Tick.Out, Tick.Options>> renderTickHandlers;
  private final ArrayList<EventHandler<Tick.In, Tick.Out, Tick.Options>> clientTickHandlers;
  private final ArrayList<EventHandler<GuiScreenMouse.In, GuiScreenMouse.Out, GuiScreenMouse.Options>> guiScreenMouseHandlers;

  private Integer mouseStartX = null;
  private Integer mouseStartY = null;

  public ForgeEventService(Minecraft minecraft) {
    this.minecraft = minecraft;

    this.openGuiModListHandlers = new ArrayList<>();
    this.openGuiIngameMenuHandlers = new ArrayList<>();
    this.renderGameOverlayHandlers = new ArrayList<>();
    this.renderChatGameOverlayHandlers = new ArrayList<>();
    this.renderTickHandlers = new ArrayList<>();
    this.clientTickHandlers = new ArrayList<>();
    this.guiScreenMouseHandlers = new ArrayList<>();
  }

  public void onOpenGuiModList(Function<OpenGui.In, OpenGui.Out> handler, OpenGui.Options options) {
    this.openGuiModListHandlers.add(new EventHandler<>(handler, options));
  }

  public void onOpenGuiIngameMenu(Function<OpenGui.In, OpenGui.Out> handler, OpenGui.Options options) {
    this.openGuiIngameMenuHandlers.add(new EventHandler<>(handler, options));
  }

  public void onRenderGameOverlay(Function<RenderGameOverlay.In, RenderGameOverlay.Out> handler, @Nonnull RenderGameOverlay.Options options) {
    this.renderGameOverlayHandlers.add(new EventHandler<>(handler, options));
  }

  /** Fires when the main chat box GUI component is rendered. */
  public void onRenderChatGameOverlay(Function<RenderChatGameOverlay.In, RenderChatGameOverlay.Out> handler, RenderChatGameOverlay.Options options) {
    this.renderChatGameOverlayHandlers.add(new EventHandler<>(handler, options));
  }

  public void onRenderTick(Function<Tick.In, Tick.Out> handler, @Nullable Tick.Options options) {
    this.renderTickHandlers.add(new EventHandler<>(handler, options));
  }

  public void onClientTick(Function<Tick.In, Tick.Out> handler, @Nullable Tick.Options options) {
    this.clientTickHandlers.add(new EventHandler<>(handler, options));
  }

  /** Fires for left-click mouse events within a GUI screen. */
  public void onGuiScreenMouse(Object key, Function<GuiScreenMouse.In, GuiScreenMouse.Out> handler, @Nonnull GuiScreenMouse.Options options) {
    this.guiScreenMouseHandlers.add(new EventHandler<>(handler, options, key));
    System.out.println(this.guiScreenMouseHandlers.size());
  }

  public boolean offGuiScreenMouse(Object key) {
    return this.removeListener(this.guiScreenMouseHandlers, key);
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiOpenEvent event) {
    ArrayList<EventHandler<OpenGui.In, OpenGui.Out, OpenGui.Options>> handlers;
    if (event.gui instanceof GuiModList) {
      handlers = this.openGuiModListHandlers;
    } else if (event.gui instanceof GuiIngameMenu) {
      handlers = this.openGuiIngameMenuHandlers;
    } else {
      return;
    }

    boolean guiReplaced = false;
    for (EventHandler<OpenGui.In, OpenGui.Out, OpenGui.Options> handler : handlers) {
      OpenGui.In eventIn = new OpenGui.In(event.gui, guiReplaced);
      OpenGui.Out eventOut = handler.callback.apply(eventIn);

      if (eventOut != null && eventOut.shouldReplaceGui) {
        guiReplaced = true;
        event.gui = eventOut.gui;
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(ClientChatReceivedEvent event) {
    /*
    https://github.com/MinecraftForge/MinecraftForge/blob/1.8.9/src/main/java/net/minecraftforge/client/event/ClientChatReceivedEvent.java
     * 0 : Standard Text Message
     * 1 : 'System' message, displayed as standard text.
     * 2 : 'Status' message, displayed above action bar, where song notifications are.
     */

    // note that some events have inner classes (i.e. additional events)

    // this is where many of the events are generated:
    // https://github.com/MinecraftForge/MinecraftForge/blob/d06e0ad71b8471923cc809dde58251de8299a143/src/main/java/net/minecraftforge/client/ForgeHooksClient.java

//    System.out.println(event.type);
//    System.out.println(event.message.getUnformattedText());
  }

  // as seen in https://github.com/MinecraftForge/MinecraftForge/blob/1.8.9/src/main/java/net/minecraftforge/client/GuiIngameForge.java
  // the Pre(TYPE) event is fired first, and default rendering is excluded if that function call returns true.
  // Once all default rendering has been completed, Post(TYPE) is fired.
  // ElementType.ALL refers to the "main render". Every main render may additionally render any of the other ElementTypes.
  //
  // writing text to the screen will place it only on top of the in-game GUI (underneath e.g. menu overlays)
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(RenderGameOverlayEvent event)
  {
    // this is a bit naughty, but pretend there is no render event when the F3 screen is open.
    // we should probably just let the handlers decide for themselves if they want to render or not.
    if (this.minecraft.gameSettings.showDebugInfo) {
      return;
    }

    for (EventHandler<RenderGameOverlay.In, RenderGameOverlay.Out, RenderGameOverlay.Options> handler : this.renderGameOverlayHandlers) {
      if (handler.options.subscribeToTypes.contains(event.type)) {
        RenderGameOverlay.In eventIn = new RenderGameOverlay.In(event.type);
        RenderGameOverlay.Out eventOut = handler.callback.apply(eventIn);
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(RenderGameOverlayEvent.Chat event) {
    int posX = event.posX;
    int posY = event.posY;

    for (EventHandler<RenderChatGameOverlay.In, RenderChatGameOverlay.Out, RenderChatGameOverlay.Options> handler : this.renderChatGameOverlayHandlers) {
      RenderChatGameOverlay.In eventIn = new RenderChatGameOverlay.In(posX, posY);
      RenderChatGameOverlay.Out eventOut = handler.callback.apply(eventIn);

      if (eventOut.newPosX != null) {
        posX = eventOut.newPosX;
      }
      if (eventOut.newPosY != null) {
        posY = eventOut.newPosY;
      }
    }

    event.posX = posX;
    event.posY = posY;
  }

  // similar to RenderGameOverlayEvent.ALL, except fires even in menus
  // writing text to the screen will put the text on TOP of everything.
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(TickEvent.RenderTickEvent event) {
    for (EventHandler<Tick.In, Tick.Out, Tick.Options> handler : this.renderTickHandlers) {
      Tick.In eventIn = new Tick.In();
      Tick.Out eventOut = handler.callback.apply(eventIn);
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(TickEvent.ClientTickEvent event) {
    for (EventHandler<Tick.In, Tick.Out, Tick.Options> handler : this.clientTickHandlers) {
      Tick.In eventIn = new Tick.In();
      Tick.Out eventOut = handler.callback.apply(eventIn);
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiScreenEvent.MouseInputEvent.Post event) {
    // todo: create a proper mouse handler that can track the whole drag sequence + multiple buttons
    // http://legacy.lwjgl.org/javadoc/org/lwjgl/input/Mouse.html

    // turns out the the `Mouse` object measures pixels from the bottom left corner. We have to apply the correct GUI
    // scaling and invert the y position.
    // see https://www.tabnine.com/web/assistant/code/rs/5c779284df79be0001d363ba#L100 for an example.
    // due to integer rounding, we can't use the `dx` and `dy` values. Instead, keep track of the starting and current
    // position of a mouse sequence (drag).
    int currentX = Mouse.getEventX() * event.gui.width / this.minecraft.displayWidth;
    int currentY = event.gui.height - Mouse.getEventY() * event.gui.height / this.minecraft.displayHeight - 1;
    int startX = this.mouseStartX == null ? currentX : this.mouseStartX;
    int startY = this.mouseStartY == null ? currentY : this.mouseStartY;

    GuiScreenMouse.In eventIn;
    if (Mouse.getEventButton() != 0) {
      // move/drag
      boolean isDragging = Mouse.isButtonDown(0);
      eventIn = new GuiScreenMouse.In(startX, startY, currentX, currentY, isDragging);
    } else {
      // click
      boolean isMouseDownEvent = Mouse.getEventButtonState();
      eventIn = new GuiScreenMouse.In(isMouseDownEvent, startX, startY, currentX, currentY);

      if (!isMouseDownEvent) {
        this.mouseStartX = null;
        this.mouseStartY = null;
      } else {
        this.mouseStartX = startX;
        this.mouseStartY = startY;
      }
    }

    for (EventHandler<GuiScreenMouse.In, GuiScreenMouse.Out, GuiScreenMouse.Options> handler : this.guiScreenMouseHandlers) {
      if (handler.options.guiScreenClass.isInstance(event.gui)) {
        GuiScreenMouse.Out eventOut = handler.callback.apply(eventIn);
      }
    }
  }

  // happy scrolling
  private <In extends Base.EventIn, Out extends Base.EventOut, Options extends Base.EventOptions> boolean removeListener(ArrayList<EventHandler<In, Out, Options>> handlers, Object key) {
    Optional<EventHandler<In, Out, Options>> handler = handlers.stream().filter(h -> h.isHandlerForKey(key)).findFirst();

    if (handler.isPresent()) {
      handlers.remove(handler.get());
      return true;
    } else {
      return false;
    }
  }

  private static class EventHandler<In extends Base.EventIn, Out extends Base.EventOut, Options extends Base.EventOptions> {
    private final Function<In, Out> callback;
    private final Options options;
    private final Object key;

    public EventHandler(Function<In, Out> callback, Options options) {
      this.callback = callback;
      this.options = options;
      this.key = new Object();
    }

    public EventHandler(Function<In, Out> callback, Options options, Object key) {
      this.callback = callback;
      this.options = options;
      this.key = key;
    }

    // we can't compare by callback because lambdas cannot be expected to follow reference equality.
    public boolean isHandlerForKey(Object key) {
      return this.key == key;
    }
  }
}
