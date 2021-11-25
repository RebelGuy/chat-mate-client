package dev.rebel.chatoverlay.services;

import dev.rebel.chatoverlay.models.chat.ChatItem;
import dev.rebel.chatoverlay.models.chat.PartialChatMessage;
import dev.rebel.chatoverlay.models.chat.PartialChatMessageType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class McChatService {
  private static final ChatStyle viewerRankStyle = new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(true);
  private static final ChatStyle viewerNameStyle = new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(false);
  private static final ChatStyle ytChatMessageTextStyle = new ChatStyle().setColor(EnumChatFormatting.WHITE);
  private static final ChatStyle ytChatMessageEmojiStyle = new ChatStyle().setColor(EnumChatFormatting.GRAY);

  private final LoggingService loggingService;
  private final FilterService filterService;


  public McChatService(LoggingService loggingService, FilterService filterService) {
    this.loggingService = loggingService;
    this.filterService = filterService;
  }

  public void addToMcChat(ChatItem item) {
    GuiIngame gui = Minecraft.getMinecraft().ingameGUI;

    if (gui != null) {
      // todo: ensure special characters (e.g. normal emojis) don't break this, as well as very long chat (might need to break up string)
      try {
        IChatComponent rank = styledText("VIEWER", viewerRankStyle);
        IChatComponent player = styledText(item.author.name, viewerNameStyle);
        List<IChatComponent> msgComponents = this.ytChatToMcChat(item, gui.getFontRenderer());

        ArrayList<IChatComponent> components = new ArrayList<>();
        components.add(rank);
        components.add(player);
        components.add(join("", msgComponents));
        IChatComponent result = join(" ", components);

        gui.getChatGUI().printChatMessage(result);
      } catch (Exception e) {
        // ignore error because it's not critical
        // todo: log error
      }
    }
  }

  private List<IChatComponent> ytChatToMcChat(ChatItem item, FontRenderer fontRenderer) throws Exception {
    ArrayList<IChatComponent> components = new ArrayList<>();

    @Nullable PartialChatMessageType prevType = null;
    @Nullable String prevText = null;
    for (PartialChatMessage msg: item.messageParts) {
      String text;
      ChatStyle style;
      if (msg.type == PartialChatMessageType.text) {
        text = this.filterService.filterNaughtyWords(msg.text);
        style = ytChatMessageTextStyle.setBold(msg.isBold).setItalic(msg.isItalics);

      } else if (msg.type == PartialChatMessageType.emoji) {
        String name = msg.name;
        char[] nameChars = name.toCharArray();

        // the name could be the literal emoji unicode character
        // check if the resource pack supports it - if so, use it!
        if (nameChars.length == 1 && fontRenderer.getCharWidth(nameChars[0]) > 0) {
          text = name;
          style = ytChatMessageTextStyle;
        } else {
          text = msg.label; // e.g. :slightly_smiling:
          style = ytChatMessageEmojiStyle;
        }

      } else throw new Exception("Invalid partial message type " + msg.type);

      // add space between components except when we have two text types after one another.
      if (prevType != null && !(msg.type == PartialChatMessageType.text && prevType == PartialChatMessageType.text)) {
        if (text.startsWith(" ") || (prevText != null && prevText.endsWith(" "))) {
          // already spaced out
        } else {
          text = " " + text;
        }
      }
      components.add(styledText(text, style));

      prevType = msg.type;
      prevText = text;
    }

    return components;
  }

  private static IChatComponent styledText(String text, ChatStyle styles) {
    IChatComponent component = new ChatComponentText(text);
    component.setChatStyle(styles);
    return component;
  }

  private static IChatComponent join(String joinText, List<IChatComponent> components) {
    IChatComponent result = new ChatComponentText("");

    for (IChatComponent comp: components) {
      result = result.appendSibling(comp);

      if (joinText.length() > 0) {
        result = result.appendText(joinText);
      }
    }

    return result;
  }
}
