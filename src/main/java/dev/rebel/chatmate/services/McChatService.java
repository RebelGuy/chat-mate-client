package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.models.chat.GetChatResponse.PartialChatMessage;
import dev.rebel.chatmate.models.chat.PartialChatMessageType;
import dev.rebel.chatmate.services.events.ChatMateEventService;
import dev.rebel.chatmate.services.events.models.LevelUpEventData;
import dev.rebel.chatmate.services.events.models.LevelUpEventData.In;
import dev.rebel.chatmate.services.events.models.LevelUpEventData.Out;
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

import static dev.rebel.chatmate.models.Styles.*;
import static dev.rebel.chatmate.services.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.services.util.ChatHelpers.styledTextWithMask;

public class McChatService {
  private final Minecraft minecraft;
  private final LoggingService loggingService;
  private final FilterService filterService;
  private final SoundService soundService;
  private final ChatMateEventService chatMateEventService;
  private final MessageService messageService;

  public McChatService(Minecraft minecraft,
                       LoggingService loggingService,
                       FilterService filterService,
                       SoundService soundService,
                       ChatMateEventService chatMateEventService,
                       MessageService messageService) {
    this.minecraft = minecraft;
    this.loggingService = loggingService;
    this.filterService = filterService;
    this.soundService = soundService;
    this.chatMateEventService = chatMateEventService;
    this.messageService = messageService;

    this.chatMateEventService.onLevelUp(this::onLevelUp, null);
  }

  public void printStreamChatItem(ChatItem item) {
    GuiIngame gui = this.minecraft.ingameGUI;

    if (gui != null) {
      try {
        Integer lvl = item.author.level;
        IChatComponent level = styledText(lvl.toString(), getLevelStyle(lvl));
        IChatComponent rank = styledText("VIEWER", VIEWER_RANK_STYLE);
        IChatComponent player = styledText(item.author.name, VIEWER_NAME_STYLE);
        McChatResult mcChatResult = this.ytChatToMcChat(item, gui.getFontRenderer());

        ArrayList<IChatComponent> components = new ArrayList<>();
        components.add(level);
        components.add(rank);
        components.add(player);
        components.add(joinComponents("", mcChatResult.chatComponents));
        IChatComponent result = joinComponents(" ", components);

        gui.getChatGUI().printChatMessage(result);

        if (mcChatResult.includesMention) {
          this.soundService.playDing();
        }
      } catch (Exception e) {
        // ignore error because it's not critical
        this.loggingService.log("[McChatService] Could not print chat message: " + e.getMessage());
      }
    }
  }

  public LevelUpEventData.Out onLevelUp(LevelUpEventData.In in) {
    GuiIngame gui = this.minecraft.ingameGUI;
    if (gui == null || in.newLevel == 0 || in.newLevel % 5 != 0) {
      return new LevelUpEventData.Out();
    }

    try {
      IChatComponent message;
      if (in.newLevel % 20 == 0) {
        message = this.messageService.getLargeLevelUpMessage(in.channelName, in.newLevel);
        this.soundService.playLevelUp(1 - in.newLevel / 200.0f);
      } else {
        message = this.messageService.getSmallLevelUpMessage(in.channelName, in.newLevel);
        this.soundService.playLevelUp(2);
      }

      gui.getChatGUI().printChatMessage(message);
    } catch (Exception e) {
      // ignore error because it's not critical
      this.loggingService.log("[McChatService] Could not print level up message: " + e.getMessage());
    }

    return new LevelUpEventData.Out();
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
        style = YT_CHAT_MESSAGE_TEXT_STYLE.setBold(msg.isBold).setItalic(msg.isItalics);

      } else if (msg.type == PartialChatMessageType.emoji) {
        String name = msg.name;
        char[] nameChars = name.toCharArray();

        // the name could be the literal emoji unicode character
        // check if the resource pack supports it - if so, use it!
        if (nameChars.length == 1 && fontRenderer.getCharWidth(nameChars[0]) > 0) {
          text = name;
          style = YT_CHAT_MESSAGE_TEXT_STYLE;
        } else {
          text = msg.label; // e.g. :slightly_smiling:
          style = YT_CHAT_MESSAGE_EMOJI_STYLE;
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
        components.addAll(styledTextWithMask(text, style, mentionMask, MENTION_TEXT_STYLE));

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

  private class McChatResult {
    public final List<IChatComponent> chatComponents;
    public final boolean includesMention;

    private McChatResult(List<IChatComponent> chatComponents, boolean includesMention) {
      this.chatComponents = chatComponents;
      this.includesMention = includesMention;
    }
  }
}
