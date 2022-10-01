package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.ForgeEventService.Events;
import dev.rebel.chatmate.services.events.models.*;
import dev.rebel.chatmate.services.util.EnumHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

// why? because I would like to keep Forge event subscriptions centralised for an easier overview and for easier debugging.
// it also makes testing specific events A LOT easier because we can just mock out this class and test the event handler
// in complete isolation.
// thank Java for the verbose typings.
public class ForgeEventService extends EventServiceBase<Events> {
  private final Minecraft minecraft;

  private int prevDisplayWidth;
  private int prevDisplayHeight;

  public ForgeEventService(LogService logService, Minecraft minecraft) {
    super(Events.class, logService);
    this.minecraft = minecraft;

    this.prevDisplayHeight = this.minecraft.displayHeight;
    this.prevDisplayWidth = this.minecraft.displayWidth;
  }

  public void onOpenGuiModList(Function<OpenGui.In, OpenGui.Out> handler, OpenGui.Options options) {
    this.addListener(Events.OpenGuiModList, handler, options);
  }

  public void onOpenGuiIngameMenu(Function<OpenGui.In, OpenGui.Out> handler, OpenGui.Options options) {
    this.addListener(Events.OpenGuiInGameMenu, handler, options);
  }

  public void onOpenChatSettingsMenu(Function<OpenGui.In, OpenGui.Out> handler, OpenGui.Options options) {
    this.addListener(Events.OpenChatSettingsMenu, handler, options);
  }

  /** Fires when the GuiChat (GuiScreen) is shown. */
  public void onOpenChat(Function<OpenGui.In, OpenGui.Out> handler, OpenGui.Options options) {
    this.addListener(Events.OpenChat, handler, options);
  }

  /** Fires after the minecraft.currentScreen has changed reference. Occurs AFTER any onOpen* events - it is read-only. */
  public void onGuiScreenChanged(Function<GuiScreenChanged.In, GuiScreenChanged.Out> handler, @Nullable GuiScreenChanged.Options options) {
    this.addListener(Events.GuiScreenChanged, handler, options);
  }

  public void onRenderGameOverlay(Function<RenderGameOverlay.In, RenderGameOverlay.Out> handler, @Nonnull RenderGameOverlay.Options options) {
    this.addListener(Events.RenderGameOverlay, handler, options);
  }

  /** Fires before the main chat box GUI component is rendered. */
  public void onRenderChatGameOverlay(Function<RenderChatGameOverlay.In, RenderChatGameOverlay.Out> handler, @Nullable RenderGameOverlay.Options options) {
    this.addListener(Events.RenderChatGameOverlay, handler, options);
  }

  public void onRenderTick(Function<Tick.In, Tick.Out> handler, @Nullable Tick.Options options) {
    this.addListener(Events.RenderTick, handler, options);
  }

  public void onClientTick(Function<Tick.In, Tick.Out> handler, @Nullable Tick.Options options) {
    this.addListener(Events.ClientTick, handler, options);
  }

  /** Fires for mouse events within a GUI screen. */
  public void onGuiScreenMouse(Function<InputEventData.In, InputEventData.Out> handler, @Nullable InputEventData.Options options) {
    this.addListener(Events.GuiScreenMouse, handler, options);
  }

  /** Fires for keyboard events within a GUI screen. */
  public void onGuiScreenKeyboard(Function<InputEventData.In, InputEventData.Out> handler, @Nullable InputEventData.Options options) {
    this.addListener(Events.GuiScreenKeyboard, handler, options);
  }

  /** Stored as a weak reference - lambda forbidden. */
  public void onScreenResize(Function<ScreenResizeData.In, ScreenResizeData.Out> handler, @Nullable ScreenResizeData.Options options, Object key) {
    this.addListener(Events.ScreenResize, handler, options, key);
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiOpenEvent event) {
    GuiScreen originalScreen = this.minecraft.currentScreen;
    Events eventType;
    if (event.gui instanceof GuiModList) {
      eventType = Events.OpenGuiModList;
    } else if (event.gui instanceof GuiIngameMenu) {
      eventType = Events.OpenGuiInGameMenu;
    } else if (event.gui instanceof ScreenChatOptions) {
      eventType = Events.OpenChatSettingsMenu;
    } else if (event.gui instanceof GuiChat) {
      eventType = Events.OpenChat;
    } else {
      eventType = null;
    }

    if (eventType != null) {
      boolean guiReplaced = false;
      for (EventHandler<OpenGui.In, OpenGui.Out, OpenGui.Options> handler : this.getListeners(eventType, OpenGui.class)) {
        OpenGui.In eventIn = new OpenGui.In(event.gui, guiReplaced);
        OpenGui.Out eventOut = this.safeDispatch(eventType, handler, eventIn);

        if (eventOut != null && eventOut.shouldReplaceGui) {
          guiReplaced = true;
          event.gui = eventOut.gui;
        }
      }
    }

    GuiScreen newScreen = event.gui;
    if (originalScreen != newScreen) {
      for (EventHandler<GuiScreenChanged.In, GuiScreenChanged.Out, GuiScreenChanged.Options> handler : this.getListeners(Events.GuiScreenChanged, GuiScreenChanged.class)) {
        GuiScreenChanged.Options options = handler.options;

        // check if the subscriber is interested in this event, and skip notifying them if not
        if (options != null) {
          Class<? extends GuiScreen> filter = options.screenFilter;
          GuiScreenChanged.ListenType listenType = options.listenType;

          boolean matchClose, matchOpen;
          if (filter == null) {
            matchClose = originalScreen == null;
            matchOpen = newScreen == null;
          } else {
            matchClose = originalScreen != null && filter.isAssignableFrom(originalScreen.getClass());
            matchOpen = newScreen != null && filter.isAssignableFrom(newScreen.getClass());
          }

          if (listenType == GuiScreenChanged.ListenType.OPEN_ONLY) {
            if (!matchOpen) continue;
          } else if (listenType == GuiScreenChanged.ListenType.CLOSE_ONLY) {
            if (!matchClose) continue;
          } else if (listenType == GuiScreenChanged.ListenType.OPEN_AND_CLOSE) {
            if (!matchOpen && !matchClose) continue;
          } else {
            throw EnumHelpers.<GuiScreenChanged.ListenType>assertUnreachable(listenType);
          }
        }

        GuiScreenChanged.In eventIn = new GuiScreenChanged.In(originalScreen, newScreen);
        GuiScreenChanged.Out eventOut = this.safeDispatch(Events.GuiScreenChanged, handler, eventIn);
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
  public void forgeEventSubscriber(RenderGameOverlayEvent.Post event) // need to subscribe to post so what we draw on top of what is already shown
  {
    // this is a bit naughty, but pretend there is no render event when the F3 screen is open.
    // we should probably just let the handlers decide for themselves if they want to render or not.
    if (this.minecraft.gameSettings.showDebugInfo) {
      return;
    }

    Events eventType = Events.RenderGameOverlay;
    for (EventHandler<RenderGameOverlay.In, RenderGameOverlay.Out, RenderGameOverlay.Options> handler : this.getListeners(eventType, RenderGameOverlay.class)) {
      if (handler.options.subscribeToTypes.contains(event.type)) {
        RenderGameOverlay.In eventIn = new RenderGameOverlay.In(event.type);
        RenderGameOverlay.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(RenderGameOverlayEvent.Chat event) {
    // fired by GuiIngameForge::renderChat
    Events eventType = Events.RenderChatGameOverlay;
    for (EventHandler<RenderChatGameOverlay.In, RenderChatGameOverlay.Out, RenderChatGameOverlay.Options> handler : this.getListeners(eventType, RenderChatGameOverlay.class)) {
      RenderChatGameOverlay.In eventIn = new RenderChatGameOverlay.In(event);
      RenderChatGameOverlay.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
    }
  }

  // similar to RenderGameOverlayEvent.ALL, except fires even in menus
  // writing text to the screen will put the text on TOP of everything.
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(TickEvent.RenderTickEvent event) {
    // check if the screen was resized every frame
    int displayWidth = this.minecraft.displayWidth;
    int displayHeight = this.minecraft.displayHeight;
    if (this.prevDisplayWidth != displayWidth || this.prevDisplayHeight != displayHeight) {
      this.prevDisplayWidth = displayWidth;
      this.prevDisplayHeight = displayHeight;

      for (EventHandler<ScreenResizeData.In, ScreenResizeData.Out, ScreenResizeData.Options> handler : this.getListeners(Events.ScreenResize, ScreenResizeData.class)) {
        ScreenResizeData.In eventIn = new ScreenResizeData.In(displayWidth, displayHeight);
        ScreenResizeData.Out eventOut = this.safeDispatch(Events.ScreenResize, handler, eventIn);
      }
    }

    Events eventType = Events.RenderTick;
    for (EventHandler<Tick.In, Tick.Out, Tick.Options> handler : this.getListeners(eventType, Tick.class)) {
      Tick.In eventIn = new Tick.In();
      Tick.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
    }

    // HACK - something is disabling alpha, but Forge expects alpha to be enabled else it causes problems when rendering textures in the server menu. reset it here after we are done rendering all our custom stuff.
    // another example why OpenGL's state machine is fucking dumb
    GlStateManager.enableAlpha();
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(TickEvent.ClientTickEvent event) {
    Events eventType = Events.ClientTick;
    for (EventHandler<Tick.In, Tick.Out, Tick.Options> handler : this.getListeners(eventType, Tick.class)) {
      Tick.In eventIn = new Tick.In();
      Tick.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
    }
  }

  // note:
  // * GuiScreenEvent.MouseInputEvent/KeyboardInputEvent: Fired from the GUI once it has been informed to handle input.
  // * InputEvent.MouseInputEvent/KeyboardInputEvent: Fired for any events that haven't already been fired by screens
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiScreenEvent.MouseInputEvent.Pre event) {
    Events eventType = Events.GuiScreenMouse;
    for (EventHandler<InputEventData.In, InputEventData.Out, InputEventData.Options> handler : this.getListeners(eventType, InputEventData.class)) {
      InputEventData.In eventIn = new InputEventData.In();
      InputEventData.Out eventOut = this.safeDispatch(eventType, handler, eventIn);

      if (eventOut != null && eventOut.cancelled) {
        event.setCanceled(true);
        return;
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiScreenEvent.KeyboardInputEvent.Pre event) {
    Events eventType = Events.GuiScreenKeyboard;
    for (EventHandler<InputEventData.In, InputEventData.Out, InputEventData.Options> handler : this.getListeners(eventType, InputEventData.class)) {
      InputEventData.In eventIn = new InputEventData.In();
      InputEventData.Out eventOut = this.safeDispatch(eventType, handler, eventIn);

      if (eventOut != null && eventOut.cancelled) {
        event.setCanceled(true);
        return;
      }
    }
  }

  public enum Events {
    OpenGuiModList,
    OpenGuiInGameMenu,
    OpenChatSettingsMenu,
    OpenChat,
    GuiScreenChanged,
    RenderGameOverlay,
    RenderChatGameOverlay,
    RenderTick,
    ClientTick,
    GuiScreenMouse,
    GuiScreenKeyboard,
    ScreenResize
  }
}
