package dev.rebel.chatmate.models;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class Styles {
  public static final ChatStyle VIEWER_RANK_STYLE = new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(true);
  public static final ChatStyle VIEWER_NAME_STYLE = new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(false);
  public static final ChatStyle YT_CHAT_MESSAGE_TEXT_STYLE = new ChatStyle().setColor(EnumChatFormatting.WHITE);
  public static final ChatStyle YT_CHAT_MESSAGE_EMOJI_STYLE = new ChatStyle().setColor(EnumChatFormatting.GRAY);
  public static final ChatStyle MENTION_TEXT_STYLE = new ChatStyle().setColor(EnumChatFormatting.GOLD);

  public static final ChatStyle LEVEL_0_TO_19 = new ChatStyle().setColor(EnumChatFormatting.GRAY);
  public static final ChatStyle LEVEL_20_TO_39 = new ChatStyle().setColor(EnumChatFormatting.BLUE);
  public static final ChatStyle LEVEL_40_TO_59 = new ChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
  public static final ChatStyle LEVEL_60_TO_79 = new ChatStyle().setColor(EnumChatFormatting.GOLD);
  public static final ChatStyle LEVEL_80_TO_99 = new ChatStyle().setColor(EnumChatFormatting.RED);
  public static final ChatStyle LEVEL_100_UPWARDS = new ChatStyle().setColor(EnumChatFormatting.BLACK);

  public static final ChatStyle INFO_MSG_STYLE = new ChatStyle().setColor(EnumChatFormatting.GRAY);
  public static final ChatStyle INFO_MSG_PREFIX_STYLE = new ChatStyle().setColor(EnumChatFormatting.BLUE);

  public static final ChatStyle LEVEL_UP_MESSAGE_BODY = new ChatStyle().setColor(EnumChatFormatting.WHITE);

  public static ChatStyle getLevelStyle(Integer level) {
    if (level < 20) {
      return LEVEL_0_TO_19;
    } else if (level < 40) {
      return LEVEL_20_TO_39;
    } else if (level < 60) {
      return LEVEL_40_TO_59;
    } else if (level < 80) {
      return LEVEL_60_TO_79;
    } else if (level < 100) {
      return LEVEL_80_TO_99;
    } else {
      return LEVEL_100_UPWARDS;
    }
  }

  public static IChatComponent styledText(String text, ChatStyle styles) {
    IChatComponent component = new ChatComponentText(text);
    component.setChatStyle(styles);
    return component;
  }
}
