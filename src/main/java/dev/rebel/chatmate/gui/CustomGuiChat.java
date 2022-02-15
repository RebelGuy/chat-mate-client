package dev.rebel.chatmate.gui;

import com.google.common.collect.Sets;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.util.ChatHelpers.ClickEventWithCallback;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.stream.GuiTwitchUserMode;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import tv.twitch.chat.ChatUserInfo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class CustomGuiChat extends GuiChat {
  private static final Logger LOGGER = LogManager.getLogger();
  private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");

  private URI clickedLinkURI;

  public CustomGuiChat(String defaultInput) {
    super(defaultInput);
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

  /** Stolen from GuiScreen. */
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
}
