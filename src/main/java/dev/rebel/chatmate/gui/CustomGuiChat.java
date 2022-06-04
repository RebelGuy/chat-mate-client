package dev.rebel.chatmate.gui;

import com.google.common.collect.Sets;
import dev.rebel.chatmate.gui.chat.ContainerChatComponent;
import dev.rebel.chatmate.gui.models.AbstractChatLine;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.services.ContextMenuService;
import dev.rebel.chatmate.services.CursorService;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.services.MinecraftProxyService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import dev.rebel.chatmate.services.events.models.MouseEventData.In.MouseButtonData.MouseButton;
import dev.rebel.chatmate.services.util.ChatHelpers.ClickEventWithCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import tv.twitch.chat.ChatUserInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.IntBuffer;
import java.util.Set;
import java.util.function.Function;

public class CustomGuiChat extends GuiChat {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");

  private final MinecraftProxyService minecraftProxyService;
  private final MouseEventService mouseEventService;
  private final ContextMenuStore contextMenuStore;
  private final ContextMenuService contextMenuService;
  private final CursorService cursorService;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseDown = this::onMouseDown;
  private final Function<MouseEventData.In, MouseEventData.Out> onMouseMove = this::onMouseMove;

  private URI clickedLinkURI;

  public CustomGuiChat(String defaultInput,
                       MinecraftProxyService minecraftProxyService,
                       MouseEventService mouseEventService,
                       ContextMenuStore contextMenuStore,
                       ContextMenuService contextMenuService,
                       CursorService cursorService) {
    super(defaultInput);

    this.minecraftProxyService = minecraftProxyService;
    this.mouseEventService = mouseEventService;
    this.contextMenuStore = contextMenuStore;
    this.contextMenuService = contextMenuService;
    this.cursorService = cursorService;

    this.mouseEventService.on(MouseEventService.Events.MOUSE_DOWN, this.onMouseDown, new MouseEventData.Options(), this);
    this.mouseEventService.on(MouseEventService.Events.MOUSE_MOVE, this.onMouseMove, new MouseEventData.Options(), this);
  }

  private MouseEventData.Out onMouseDown(MouseEventData.In in) {
    if (!this.minecraftProxyService.checkCurrentScreen(this)) {
      return new MouseEventData.Out();
    }

    CustomGuiNewChat chatGui = this.minecraftProxyService.getChatGUI();
    chatGui.setSelectedLine(null);

    if (in.mouseButtonData.eventButton == MouseButton.RIGHT_BUTTON) {
      Dim x = in.mousePositionData.x;
      Dim y = in.mousePositionData.y;

      IChatComponent component = chatGui.getChatComponent(x, y);

      if (component instanceof ContainerChatComponent) {
        ContainerChatComponent container = (ContainerChatComponent)component;
        if (container.data instanceof PublicUser) {
          this.contextMenuService.showUserContext(x, y, (PublicUser)container.data);
          return new MouseEventData.Out(MouseEventData.Out.MouseHandlerAction.HANDLED);
        }
      }

      AbstractChatLine chatLine = chatGui.getAbstractChatLine(x, y);
      if (chatLine != null) {
        this.contextMenuService.showChatLineContext(x, y, chatLine);
        chatGui.setSelectedLine(chatLine);
      }
    }

    return new MouseEventData.Out();
  }

  private MouseEventData.Out onMouseMove(MouseEventData.In in) {
    if (!this.minecraftProxyService.checkCurrentScreen(this)) {
      return new MouseEventData.Out();
    }

    if (this.contextMenuStore.isShowingContextMenu()) {
      this.cursorService.setCursor(CursorType.DEFAULT);
      return new MouseEventData.Out();
    }

    Dim x = in.mousePositionData.x;
    Dim y = in.mousePositionData.y;
    IChatComponent component = this.minecraftProxyService.getChatGUI().getChatComponent(x, y);
    ComponentActionType action = this.getComponentActionType(component);

    if (action == ComponentActionType.CLICK) {
      this.cursorService.setCursor(CursorType.CLICK);
    } else if (action == ComponentActionType.CONTEXT) {
      this.cursorService.setCursor(CursorType.TIP);
    } else {
      this.cursorService.setCursor(CursorType.DEFAULT);
    }

    return new MouseEventData.Out();
  }

  @Override
  public void handleMouseInput() throws IOException {
    if (this.contextMenuStore.isShowingContextMenu()) {
      return;
    }

    super.handleMouseInput();
  }

  private ComponentActionType getComponentActionType(IChatComponent component) {
    if (component == null) {
      return ComponentActionType.NONE;
    }

    if (component instanceof ContainerChatComponent) {
      ContainerChatComponent container = (ContainerChatComponent)component;
      if (container.data instanceof PublicUser) {
        return ComponentActionType.CONTEXT;
      } else {
        return getComponentActionType(container.component);
      }
    } else {
      ClickEvent clickEvent = component.getChatStyle().getChatClickEvent();
      if (clickEvent instanceof ClickEventWithCallback) {
        ClickEventWithCallback clickEventWithCallback = (ClickEventWithCallback)clickEvent;
        if (clickEventWithCallback.isClickable()) {
          return ComponentActionType.CLICK;
        }
      } else if (clickEvent != null) {
        // can always click vanilla components
        return ComponentActionType.CLICK;
      }
    }

    return ComponentActionType.NONE;
  }

  /** Stolen mostly from GuiScreen, but with custom callback handler handling. */
  @Override
  public boolean handleComponentClick(IChatComponent component)
  {
    if (component == null) {
      return false;
    }

    ClickEvent clickevent = component.getChatStyle().getChatClickEvent();

    if (isShiftKeyDown()) {
      if (component.getChatStyle().getInsertion() != null) {
        this.setText(component.getChatStyle().getInsertion(), false);
      }
    } else if (clickevent != null) {
      if (ClickEventWithCallback.isClickEventWithCallback(clickevent)) {
        ClickEventWithCallback customEvent = (ClickEventWithCallback)clickevent;
        return customEvent.handleClick();

      } else if (clickevent.getAction() == ClickEvent.Action.OPEN_URL) {
        if (!this.mc.gameSettings.chatLinks) {
          return false;
        }

        try {
          URI uri = new URI(clickevent.getValue());
          String s = uri.getScheme();

          if (s == null) {
            throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
          }

          if (!PROTOCOLS.contains(s.toLowerCase())) {
            throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase());
          }

          if (this.mc.gameSettings.chatLinksPrompt) {
            this.clickedLinkURI = uri;
            this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, clickevent.getValue(), 31102009, false));
          } else {
            this.openWebLink(uri);
          }
        } catch (URISyntaxException urisyntaxexception) {
          LOGGER.error((String)("Can\'t open url for " + clickevent), (Throwable)urisyntaxexception);
        }

      } else if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE) {
        URI uri1 = (new File(clickevent.getValue())).toURI();
        this.openWebLink(uri1);

      } else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
        this.setText(clickevent.getValue(), true);

      } else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
        this.sendChatMessage(clickevent.getValue(), false);

      } else if (clickevent.getAction() == ClickEvent.Action.TWITCH_USER_INFO) {
        ChatUserInfo chatuserinfo = this.mc.getTwitchStream().func_152926_a(clickevent.getValue());

        if (chatuserinfo != null) {
          this.mc.displayGuiScreen(new GuiTwitchUserMode(this.mc.getTwitchStream(), chatuserinfo));
        } else {
          LOGGER.error("Tried to handle twitch user but couldn\'t find them!");
        }

      } else {
        LOGGER.error("Don\'t know how to handle " + clickevent);
      }

      return true;
    }

    return false;
  }

  /** Stolen from GuiScreen. */
  private void openWebLink(URI url)
  {
    try
    {
      Class<?> oclass = Class.forName("java.awt.Desktop");
      Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
      oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {url});
    }
    catch (Throwable throwable)
    {
      LOGGER.error("Couldn\'t open link", throwable);
    }
  }

  /** Stolen from GuiScreen. Needs to be reproduced here because of the use of private `this.clickedLinkURI`. */
  @Override
  public void confirmClicked(boolean result, int id)
  {
    if (id == 31102009)
    {
      if (result)
      {
        this.openWebLink(this.clickedLinkURI);
      }

      this.clickedLinkURI = null;
      this.mc.displayGuiScreen(this);
    }
  }

  @Override
  protected void handleComponentHover(IChatComponent component, int x, int y) {
    // disable hovering effects if a context menu is showing
    if (this.contextMenuStore.isShowingContextMenu()) {
      return;
    }
    super.handleComponentHover(component, x, y);
  }

  @Override
  public void onGuiClosed() {
    this.cursorService.setCursor(CursorType.DEFAULT);
    this.minecraftProxyService.getChatGUI().setSelectedLine(null);
    super.onGuiClosed();
  }

  private enum ComponentActionType {
    NONE, CLICK, CONTEXT
  }
}
