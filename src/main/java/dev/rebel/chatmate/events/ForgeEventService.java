package dev.rebel.chatmate.events;

import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.models.*;
import dev.rebel.chatmate.util.EnumHelpers;
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

// why? because I would like to keep Forge event subscriptions centralised for an easier overview and for easier debugging.
// it also makes testing specific events A LOT easier because we can just mock out this class and test the event handler
// in complete isolation.
// thank Java for the verbose typings.
public class ForgeEventService extends EventServiceBase<ForgeEventService.EventType> {
  private final Minecraft minecraft;

  private int prevDisplayWidth;
  private int prevDisplayHeight;

  public ForgeEventService(LogService logService, Minecraft minecraft) {
    super(EventType.class, logService);
    this.minecraft = minecraft;

    this.prevDisplayHeight = this.minecraft.displayHeight;
    this.prevDisplayWidth = this.minecraft.displayWidth;
  }

  public void onOpenGuiModList(EventCallback<OpenGuiEventData> handler) {
    this.addListener(EventType.OPEN_GUI_MOD_LIST, handler, null);
  }

  public void onOpenGuiIngameMenu(EventCallback<OpenGuiEventData> handler) {
    this.addListener(EventType.OPEN_GUI_IN_GAME_MENU, handler, null);
  }

  public void onOpenChatSettingsMenu(EventCallback<OpenGuiEventData> handler) {
    this.addListener(EventType.OPEN_CHAT_SETTINGS_MENU, handler, null);
  }

  /** Fires when the GuiChat (GuiScreen) is shown. */
  public void onOpenChat(EventCallback<OpenGuiEventData> handler) {
    this.addListener(EventType.OPEN_CHAT, handler, null);
  }

  /** Fires after the minecraft.currentScreen has changed reference. Occurs AFTER any onOpen* events - it is read-only. */
  public void onGuiScreenChanged(EventCallback<GuiScreenChangedEventData> handler, @Nullable GuiScreenChangedEventOptions options) {
    this.addListener(EventType.GUI_SCREEN_CHANGED, handler, options);
  }

  public void onRenderGameOverlay(EventCallback<RenderGameOverlayEventData> handler, @Nonnull RenderGameOverlayEventOptions options) {
    this.addListener(EventType.RENDER_GAME_OVERLAY, handler, options);
  }

  /** Fires before the main chat box GUI component is rendered. */
  public void onRenderChatGameOverlay(EventCallback<RenderChatGameOverlayEventData> handler, @Nullable RenderGameOverlayEventOptions options) {
    this.addListener(EventType.RENDER_CHAT_GAME_OVERLAY, handler, options);
  }

  public void onRenderTick(EventCallback<?> handler) {
    this.addListener(EventType.RENDER_TICK, handler, null);
  }

  public void onClientTick(EventCallback<?> handler) {
    this.addListener(EventType.CLIENT_TICK, handler, null);
  }

  /** Fires for mouse events within a GUI screen. */
  public void onGuiScreenMouse(EventCallback<?> handler) {
    this.addListener(EventType.GUI_SCREEN_MOUSE, handler, null);
  }

  /** Fires for keyboard events within a GUI screen. */
  public void onGuiScreenKeyboard(EventCallback<?> handler) {
    this.addListener(EventType.GUI_SCREEN_KEYBOARD, handler, null);
  }

  /** Stored as a weak reference - lambda forbidden. */
  public void onScreenResize(EventCallback<ScreenResizeData> handler, Object key) {
    this.addListener(EventType.SCREEN_RESIZE, handler, null, key);
  }

  public void off(EventType event, Object key) {
    this.removeListener(event, key);
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiOpenEvent forgeEvent) {
    GuiScreen originalScreen = this.minecraft.currentScreen;
    EventType eventType;
    if (forgeEvent.gui instanceof GuiModList) {
      eventType = EventType.OPEN_GUI_MOD_LIST;
    } else if (forgeEvent.gui instanceof GuiIngameMenu) {
      eventType = EventType.OPEN_GUI_IN_GAME_MENU;
    } else if (forgeEvent.gui instanceof ScreenChatOptions) {
      eventType = EventType.OPEN_CHAT_SETTINGS_MENU;
    } else if (forgeEvent.gui instanceof GuiChat) {
      eventType = EventType.OPEN_CHAT;
    } else {
      eventType = null;
    }

    if (eventType != null) {
      Event<OpenGuiEventData> event = new Event<>(new OpenGuiEventData(forgeEvent.gui));
      for (EventHandler<OpenGuiEventData,?> handler : this.getListeners(eventType, OpenGuiEventData.class)) {
        super.safeDispatch(eventType, handler, event);

        if (event.hasModifiedData) {
          forgeEvent.gui = event.getData().gui;
        }
        if (event.stoppedPropagation) {
          forgeEvent.setCanceled(event.stoppedPropagation);
          break;
        }
      }
    }

    GuiScreen newScreen = forgeEvent.gui;
    if (originalScreen != newScreen) {
      Event<GuiScreenChangedEventData> event = new Event<>(new GuiScreenChangedEventData(originalScreen, newScreen), false);
      for (EventHandler<GuiScreenChangedEventData, GuiScreenChangedEventOptions> handler : this.getListeners(EventType.GUI_SCREEN_CHANGED, GuiScreenChangedEventData.class, GuiScreenChangedEventOptions.class)) {
        GuiScreenChangedEventOptions options = handler.options;

        // check if the subscriber is interested in this event, and skip notifying them if not
        if (options != null) {
          Class<? extends GuiScreen> filter = options.screenFilter;
          GuiScreenChangedEventData.ListenType listenType = options.listenType;

          boolean matchClose, matchOpen;
          if (filter == null) {
            matchClose = originalScreen == null;
            matchOpen = newScreen == null;
          } else {
            matchClose = originalScreen != null && filter.isAssignableFrom(originalScreen.getClass());
            matchOpen = newScreen != null && filter.isAssignableFrom(newScreen.getClass());
          }

          if (listenType == GuiScreenChangedEventData.ListenType.OPEN_ONLY) {
            if (!matchOpen) continue;
          } else if (listenType == GuiScreenChangedEventData.ListenType.CLOSE_ONLY) {
            if (!matchClose) continue;
          } else if (listenType == GuiScreenChangedEventData.ListenType.OPEN_AND_CLOSE) {
            if (!matchOpen && !matchClose) continue;
          } else {
            throw EnumHelpers.<GuiScreenChangedEventData.ListenType>assertUnreachable(listenType);
          }
        }

        super.safeDispatch(EventType.GUI_SCREEN_CHANGED, handler, event);
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
  public void forgeEventSubscriber(RenderGameOverlayEvent.Post forgeEvent) { // need to subscribe to post so what we draw on top of what is already shown
    // this is a bit naughty, but pretend there is no render event when the F3 screen is open.
    // we should probably just let the handlers decide for themselves if they want to render or not.
    if (this.minecraft.gameSettings.showDebugInfo) {
      return;
    }

    EventType eventType = EventType.RENDER_GAME_OVERLAY;
    Event<RenderGameOverlayEventData> event = new Event<>(new RenderGameOverlayEventData(forgeEvent.type));
    for (EventHandler<RenderGameOverlayEventData, RenderGameOverlayEventOptions> handler : this.getListeners(eventType, RenderGameOverlayEventData.class, RenderGameOverlayEventOptions.class)) {
      if (handler.options.subscribeToTypes.contains(forgeEvent.type)) {
        super.safeDispatch(eventType, handler, event);

        if (event.stoppedPropagation) {
          forgeEvent.setCanceled(true);
          return;
        }
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(RenderGameOverlayEvent.Chat forgeEvent) {
    // fired by GuiIngameForge::renderChat
    EventType eventType = EventType.RENDER_CHAT_GAME_OVERLAY;
    Event<RenderChatGameOverlayEventData> event = new Event<>(new RenderChatGameOverlayEventData(forgeEvent.posX, forgeEvent.posY));
    for (EventHandler<RenderChatGameOverlayEventData, ?> handler : this.getListeners(eventType, RenderChatGameOverlayEventData.class)) {
      super.safeDispatch(eventType, handler, event);

      if (event.stoppedPropagation) {
        forgeEvent.setCanceled(true);
      }
    }
  }

  // similar to RenderGameOverlayEvent.ALL, except fires even in menus
  // writing text to the screen will put the text on TOP of everything.
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(TickEvent.RenderTickEvent forgeEvent) {
    // check if the screen was resized every frame
    int displayWidth = this.minecraft.displayWidth;
    int displayHeight = this.minecraft.displayHeight;
    if (this.prevDisplayWidth != displayWidth || this.prevDisplayHeight != displayHeight) {
      this.prevDisplayWidth = displayWidth;
      this.prevDisplayHeight = displayHeight;

      Event<ScreenResizeData> resizeEvent = new Event<>(new ScreenResizeData(displayWidth, displayHeight), false);
      for (EventHandler<ScreenResizeData, ?> handler : this.getListeners(EventType.SCREEN_RESIZE, ScreenResizeData.class)) {
        super.safeDispatch(EventType.SCREEN_RESIZE, handler, resizeEvent);
      }
    }

    EventType eventType = EventType.RENDER_TICK;
    Event<?> renderEvent = new Event<>(null, false);
    for (EventHandler<?,?> handler : this.getListeners(eventType)) {
      super.safeDispatch(eventType, handler, renderEvent);
    }

    // HACK - something is disabling alpha, but Forge expects alpha to be enabled else it causes problems when rendering textures in the server menu. reset it here after we are done rendering all our custom stuff.
    // another example why OpenGL's state machine is fucking dumb
    GlStateManager.enableAlpha();
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(TickEvent.ClientTickEvent forgeEvent) {
    EventType eventType = EventType.CLIENT_TICK;
    Event<?> event = new Event<>(null, false);
    for (EventHandler<?,?> handler : this.getListeners(eventType)) {
      super.safeDispatch(eventType, handler, event);
    }
  }

  // note:
  // * GuiScreenEvent.MouseInputEvent/KeyboardInputEvent: Fired from the GUI once it has been informed to handle input.
  // * InputEvent.MouseInputEvent/KeyboardInputEvent: Fired for any events that haven't already been fired by screens
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiScreenEvent.MouseInputEvent.Pre forgeEvent) {
    EventType eventType = EventType.GUI_SCREEN_MOUSE;
    Event<?> event = new Event<>();
    for (EventHandler<?,?> handler : this.getListeners(eventType)) {
      super.safeDispatch(eventType, handler, event);

      if (event.stoppedPropagation) {
        forgeEvent.setCanceled(true);
        return;
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiScreenEvent.KeyboardInputEvent.Pre forgeEvent) {
    EventType eventType = EventType.GUI_SCREEN_KEYBOARD;
    Event<?> event = new Event<>();
    for (EventHandler<?,?> handler : this.getListeners(eventType)) {
      super.safeDispatch(eventType, handler, event);

      if (event.stoppedPropagation) {
        forgeEvent.setCanceled(true);
        return;
      }
    }
  }

  public enum EventType {
    OPEN_GUI_MOD_LIST,
    OPEN_GUI_IN_GAME_MENU,
    OPEN_CHAT_SETTINGS_MENU,
    OPEN_CHAT,
    GUI_SCREEN_CHANGED,
    RENDER_GAME_OVERLAY,
    RENDER_CHAT_GAME_OVERLAY,
    RENDER_TICK,
    CLIENT_TICK,
    GUI_SCREEN_MOUSE,
    GUI_SCREEN_KEYBOARD,
    SCREEN_RESIZE
  }
}
