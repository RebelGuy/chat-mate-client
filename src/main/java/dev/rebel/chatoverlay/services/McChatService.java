package dev.rebel.chatoverlay.services;

import dev.rebel.chatoverlay.models.chat.ChatItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

public class McChatService {
  private static final ChatStyle viewerRankStyle = new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(true);
  private static final ChatStyle viewerNameStyle = new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(false);
  private static final ChatStyle ytChatMessageStyle = new ChatStyle().setColor(EnumChatFormatting.WHITE);


  public McChatService() {
  }

  public void addToMcChat(ChatItem item) {
    if (Minecraft.getMinecraft().ingameGUI != null) {
      // todo: ensure special characters (e.g. normal emojis) don't break this, as well as very long chat (might need to break up string)
      try {
        IChatComponent rank = styledText("VIEWER", viewerRankStyle);
        IChatComponent player = styledText(item.author.name, viewerNameStyle);
        IChatComponent message = styledText(item.renderedText, ytChatMessageStyle);
        IChatComponent result = join(" ", rank, player, message);

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(result);
      } catch (Exception e) {
        // ignore error because it's not critical
        // todo: log error
      }
    }
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
