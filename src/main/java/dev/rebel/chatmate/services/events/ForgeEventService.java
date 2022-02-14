package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.gui.CustomGuiIngame;
import dev.rebel.chatmate.gui.CustomGuiNewChat;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.ForgeEventService.Events;
import dev.rebel.chatmate.services.events.models.*;
import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOptions;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

// why? because I would like to keep Forge event subscriptions centralised for an easier overview and for easier debugging.
// it also makes testing specific events A LOT easier because we can just mock out this class and test the event handler
// in complete isolation.
// thank Java for the verbose typings.
public class ForgeEventService extends EventServiceBase<Events> {
  private final Minecraft minecraft;

  public ForgeEventService(LogService logService, Minecraft minecraft) {
    super(Events.class, logService);
    this.minecraft = minecraft;
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


  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void forgeEventSubscriber(GuiOpenEvent event) {
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
      return;
    }

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
    Events eventType = Events.RenderTick;
    for (EventHandler<Tick.In, Tick.Out, Tick.Options> handler : this.getListeners(eventType, Tick.class)) {
      Tick.In eventIn = new Tick.In();
      Tick.Out eventOut = this.safeDispatch(eventType, handler, eventIn);
    }
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
    RenderGameOverlay,
    RenderChatGameOverlay,
    RenderTick,
    ClientTick,
    GuiScreenMouse,
    GuiScreenKeyboard
  }
}
