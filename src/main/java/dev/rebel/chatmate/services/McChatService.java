package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.models.chat.GetChatResponse.PartialChatMessage;
import dev.rebel.chatmate.models.chat.PartialChatMessageType;
import dev.rebel.chatmate.services.util.TextHelpers;
import dev.rebel.chatmate.services.util.TextHelpers.StringMask;
import dev.rebel.chatmate.services.util.TextHelpers.WordFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class McChatService {
  private static final ChatStyle viewerRankStyle = new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE).setBold(true);
  private static final ChatStyle viewerNameStyle = new ChatStyle().setColor(EnumChatFormatting.YELLOW).setBold(false);
  private static final ChatStyle ytChatMessageTextStyle = new ChatStyle().setColor(EnumChatFormatting.WHITE);
  private static final ChatStyle ytChatMessageEmojiStyle = new ChatStyle().setColor(EnumChatFormatting.GRAY);
  private static final ChatStyle mentionTextStyle = new ChatStyle().setColor(EnumChatFormatting.GOLD);

  private static final ChatStyle level0to19 = new ChatStyle().setColor(EnumChatFormatting.GRAY);
  private static final ChatStyle level20to39 = new ChatStyle().setColor(EnumChatFormatting.BLUE);
  private static final ChatStyle level40to59 = new ChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
  private static final ChatStyle level60to79 = new ChatStyle().setColor(EnumChatFormatting.GOLD);
  private static final ChatStyle level80to99 = new ChatStyle().setColor(EnumChatFormatting.RED);
  private static final ChatStyle level100upwards = new ChatStyle().setColor(EnumChatFormatting.BLACK);

  private final Minecraft minecraft;
  private final LoggingService loggingService;
  private final FilterService filterService;
  private final SoundService soundService;

  public McChatService(Minecraft minecraft, LoggingService loggingService, FilterService filterService, SoundService soundService) {
    this.minecraft = minecraft;
    this.loggingService = loggingService;
    this.filterService = filterService;
    this.soundService = soundService;
  }

  public void addToMcChat(ChatItem item) {
    GuiIngame gui = this.minecraft.ingameGUI;

    if (gui != null) {
      try {
        Integer lvl = item.author.level;
        IChatComponent level = styledText(lvl.toString(), getLevelStyle(lvl));
        IChatComponent rank = styledText("VIEWER", viewerRankStyle);
        IChatComponent player = styledText(item.author.name, viewerNameStyle);
        McChatResult mcChatResult = this.ytChatToMcChat(item, gui.getFontRenderer());

        ArrayList<IChatComponent> components = new ArrayList<>();
        components.add(level);
        components.add(rank);
        components.add(player);
        components.add(join("", mcChatResult.chatComponents));
        IChatComponent result = join(" ", components);

        gui.getChatGUI().printChatMessage(result);

        if (mcChatResult.includesMention) {
          this.soundService.playDing();
        }
      } catch (Exception e) {
        // ignore error because it's not critical
        // todo: log error
        System.out.println("[McChatService] Could not print chat message: " + e.getMessage());
      }
    }
  }

  private McChatResult ytChatToMcChat(ChatItem item, FontRenderer fontRenderer) throws Exception {
    ArrayList<IChatComponent> components = new ArrayList<>();
    boolean includesMention = false;

    @Nullable PartialChatMessageType prevType = null;
    @Nullable String prevText = null;
    for (PartialChatMessage msg: item.messageParts) {
      String text;
      ChatStyle style;
      if (msg.type == PartialChatMessageType.text) {
        text = this.filterService.censorNaughtyWords(msg.text);
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

      if (msg.type == PartialChatMessageType.text) {
        WordFilter[] mentionFilter = TextHelpers.makeWordFilters("[rebel_guy]", "[rebel guy]", "[rebel]");
        StringMask mentionMask = FilterService.filterWords(text, mentionFilter);
        components.addAll(styledTextWithMask(text, style, mentionMask, mentionTextStyle));

        if (mentionMask.any()) {
          includesMention = true;
        }
      } else {
        components.add(styledText(text, style));
      }

      prevType = msg.type;
      prevText = text;
    }

    return new McChatResult(components, includesMention);
  }

  private static ChatStyle getLevelStyle(Integer level) {
    if (level < 20) {
      return level0to19;
    } else if (level < 40) {
      return level20to39;
    } else if (level < 60) {
      return level40to59;
    } else if (level < 80) {
      return level60to79;
    } else if (level < 100) {
      return level80to99;
    } else {
      return level100upwards;
    }
  }

  // overwrites the style in one or more parts of the text
  private static Collection<IChatComponent> styledTextWithMask(String text, ChatStyle baseStyle, StringMask mask, ChatStyle maskStyle) {
    Collection<IChatComponent> collection = new ArrayList<>();
    char[] chars = text.toCharArray();

    boolean isSnippetMasked = false;
    StringBuilder snippet = new StringBuilder();

    // keep collecting chars until the mask value flips
    for (int i = 0; i <= text.length(); i++) {
      if (i == text.length() || mask.mask[i] != isSnippetMasked) {
        // flush previous
        if (snippet.length() > 0) {
          ChatStyle style = isSnippetMasked ? maskStyle : baseStyle;
          collection.add(new ChatComponentText(snippet.toString()).setChatStyle(style));

          if (i == text.length()) {
            continue;
          }
        }

        snippet = new StringBuilder();
        isSnippetMasked = !isSnippetMasked;
      }

      snippet.append(chars[i]);
    }

    return collection;
  }

  private static IChatComponent styledText(String text, ChatStyle styles) {
    IChatComponent component = new ChatComponentText(text);
    component.setChatStyle(styles);
    return component;
  }

  private static IChatComponent join(String joinText, List<IChatComponent> components) {
    IChatComponent result = new ChatComponentText("");

    boolean isFirst = true;
    for (IChatComponent comp: components) {
      if (!isFirst) {
        if (joinText.length() > 0) {
          result = result.appendText(joinText);
        }
      } else {
        isFirst = false;
      }

      result = result.appendSibling(comp);
    }

    return result;
  }

  private class McChatResult {
    public final List<IChatComponent> chatComponents;
    public final boolean includesMention;

    private McChatResult(List<IChatComponent> chatComponents, boolean includesMention) {
      this.chatComponents = chatComponents;
      this.includesMention = includesMention;
    }
  }
}
