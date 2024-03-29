package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.function.Supplier;

public class Styles {
  public static final Supplier<ChatStyle> VIEWER_RANK_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(true);
  public static final Supplier<ChatStyle> VIEWER_NAME_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(false);
  public static final FontFactory VIEWER_NAME_FONT = df -> Font.fromChatStyle(VIEWER_NAME_STYLE.get(), df);
  public static final Supplier<ChatStyle> YOUTUBE_CHANNEL_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.RED).setBold(false);
  public static final Supplier<ChatStyle> TWITCH_CHANNEL_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(false);
  public static final Supplier<ChatStyle> YT_CHAT_MESSAGE_TEXT_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.WHITE);
  public static final Supplier<ChatStyle> YT_CHAT_MESSAGE_EMOJI_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.GRAY);
  public static final Supplier<ChatStyle> YT_CHAT_MESSAGE_CHEER_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.GRAY);
  public static final Supplier<ChatStyle> MENTION_TEXT_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.GOLD);

  public static final Supplier<ChatStyle> LEVEL_0_TO_19 = () -> new ChatStyle().setColor(EnumChatFormatting.GRAY);
  public static final Supplier<ChatStyle> LEVEL_20_TO_39 = () -> new ChatStyle().setColor(EnumChatFormatting.BLUE);
  public static final Supplier<ChatStyle> LEVEL_40_TO_59 = () -> new ChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
  public static final Supplier<ChatStyle> LEVEL_60_TO_79 = () -> new ChatStyle().setColor(EnumChatFormatting.GOLD);
  public static final Supplier<ChatStyle> LEVEL_80_TO_99 = () -> new ChatStyle().setColor(EnumChatFormatting.RED);
  public static final Supplier<ChatStyle> LEVEL_100_UPWARDS = () -> new ChatStyle().setColor(EnumChatFormatting.BLACK);

  public static final Supplier<ChatStyle> INFO_MSG_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.GRAY);
  public static final Supplier<ChatStyle> INFO_SUBTLE_MSG_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.GRAY).setItalic(true);
  public static final Supplier<ChatStyle> GOOD_MSG_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.GREEN);
  public static final Supplier<ChatStyle> BAD_MSG_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.RED);
  public static final Supplier<ChatStyle> HIGHLIGHT_MSG_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.YELLOW);
  public static final Supplier<ChatStyle> ERROR_MSG_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.RED);
  public static final Supplier<ChatStyle> INFO_MSG_PREFIX_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.BLUE);

  public static final Supplier<ChatStyle> INTERACTIVE_STYLE = () -> new ChatStyle().setColor(EnumChatFormatting.BLUE).setUnderlined(true);
  public static final Supplier<ChatStyle> INTERACTIVE_STYLE_DE_EMPHASISE = () -> new ChatStyle().setColor(EnumChatFormatting.DARK_GRAY).setUnderlined(true);
  public static final Supplier<ChatStyle> INTERACTIVE_STYLE_DISABLED = () -> new ChatStyle().setColor(EnumChatFormatting.DARK_GRAY).setUnderlined(false);

  public static ChatStyle getLevelStyle(Integer level) {
    if (level < 20) {
      return LEVEL_0_TO_19.get();
    } else if (level < 40) {
      return LEVEL_20_TO_39.get();
    } else if (level < 60) {
      return LEVEL_40_TO_59.get();
    } else if (level < 80) {
      return LEVEL_60_TO_79.get();
    } else if (level < 100) {
      return LEVEL_80_TO_99.get();
    } else {
      return LEVEL_100_UPWARDS.get();
    }
  }

  public static ChatComponentText styledText(String text, ChatStyle styles) {
    ChatComponentText component = new ChatComponentText(text);
    component.setChatStyle(styles);
    return component;
  }

  @FunctionalInterface
  public interface FontFactory {
    Font create(DimFactory df);
  }
}
