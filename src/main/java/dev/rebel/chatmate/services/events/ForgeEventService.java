package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.services.events.models.Base;
import dev.rebel.chatmate.services.events.models.OpenGui;
import dev.rebel.chatmate.services.events.models.RenderGameOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

  public ForgeEventService(Minecraft minecraft) {
    this.minecraft = minecraft;

    this.openGuiModListHandlers = new ArrayList<>();
    this.openGuiIngameMenuHandlers = new ArrayList<>();
    this.renderGameOverlayHandlers = new ArrayList<>();
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

  private static class EventHandler<In extends Base.EventIn, Out extends Base.EventOut, Options extends Base.EventOptions> {
    private final Function<In, Out> callback;
    private final Options options;

    public EventHandler(Function<In, Out> callback, Options options) {
      this.callback = callback;
      this.options = options;
    }
  }
}
