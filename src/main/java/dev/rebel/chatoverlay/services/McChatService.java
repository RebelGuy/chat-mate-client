package dev.rebel.chatoverlay.services;

import dev.rebel.chatoverlay.models.chat.ChatItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.Arrays;

public class McChatService {
  private static final ChatStyle viewerRankStyle = new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(true);
  private static final ChatStyle viewerNameStyle = new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(false);
  private static final ChatStyle ytChatMessageStyle = new ChatStyle().setColor(EnumChatFormatting.WHITE);

  private final FilterService filterService;


  public McChatService(FilterService filterService) {
    this.filterService = filterService;
  }

  public void addToMcChat(ChatItem item) {
    if (Minecraft.getMinecraft().ingameGUI != null) {
      // todo: ensure special characters (e.g. normal emojis) don't break this, as well as very long chat (might need to break up string)
      try {
        IChatComponent rank = styledText("VIEWER", viewerRankStyle);
        IChatComponent player = styledText(item.author.name, viewerNameStyle);

        String messageText = this.getMessageText(item);
        IChatComponent message = styledText(messageText, ytChatMessageStyle);
        IChatComponent result = join(" ", rank, player, message);

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(result);
      } catch (Exception e) {
        // ignore error because it's not critical
        // todo: log error
      }
    }
  }

  private String getMessageText(ChatItem item) {
    String rendered = item.renderedText;
    rendered = fixUnicode_Hack(rendered);
    return this.filterService.filterNaughtyWords(rendered);
  }

  private static String fixUnicode_Hack(String text) {
    StringBuilder builder = new StringBuilder();

    // have to find a way to replace multi-character things, e.g. 🙂 is '\uD83D\uDE42'
    text = text.replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", ":)");

    // replace the weird apostrophe character
    text = text.replaceAll("‘", "'");

    // remove everything else after code 127
    for (char c: text.toCharArray()) {
      if (c <= 127) {
        builder.append(c);
      }
    }

    return builder.toString();
  }

  private static IChatComponent styledText(String text, ChatStyle styles) {
    IChatComponent component = new ChatComponentText(text);
    component.setChatStyle(styles);
    return component;
  }

  private static IChatComponent join(String joinText, IChatComponent... components) {
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
