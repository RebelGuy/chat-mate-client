package dev.rebel.chatmate.services;

import dev.rebel.chatmate.gui.chat.*;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.models.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.models.publicObjects.chat.PublicMessagePart.MessagePartType;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.models.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.models.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.services.events.ChatMateEventService;
import dev.rebel.chatmate.services.events.models.LevelUpEventData;
import dev.rebel.chatmate.services.events.models.NewTwitchFollowerEventData;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.TextHelpers;
import dev.rebel.chatmate.services.util.TextHelpers.StringMask;
import dev.rebel.chatmate.services.util.TextHelpers.WordFilter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static dev.rebel.chatmate.models.Styles.*;
import static dev.rebel.chatmate.proxy.EndpointProxy.getApiErrorMessage;
import static dev.rebel.chatmate.services.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.services.util.ChatHelpers.styledTextWithMask;

public class McChatService {
  private final MinecraftProxyService minecraftProxyService;
  private final LogService logService;
  private final FilterService filterService;
  private final SoundService soundService;
  private final ChatMateEventService chatMateEventService;
  private final MessageService messageService;
  private final ImageService imageService;
  private final Config config;

  public McChatService(MinecraftProxyService minecraftProxyService,
                       LogService logService,
                       FilterService filterService,
                       SoundService soundService,
                       ChatMateEventService chatMateEventService,
                       MessageService messageService,
                       ImageService imageService,
                       Config config) {
    this.minecraftProxyService = minecraftProxyService;
    this.logService = logService;
    this.filterService = filterService;
    this.soundService = soundService;
    this.chatMateEventService = chatMateEventService;
    this.messageService = messageService;
    this.imageService = imageService;
    this.config = config;

    this.chatMateEventService.onLevelUp(this::onLevelUp, null);
    this.chatMateEventService.onNewTwitchFollower(this::onNewTwitchFollower, null);
    this.config.getIdentifyPlatforms().onChange(_value -> this.minecraftProxyService.refreshChat());
  }

  public void printStreamChatItem(PublicChatItem item) {
    PublicUserRank[] activePunishments = item.author.getActivePunishments();
    if (activePunishments.length > 0) {
      String name = item.author.userInfo.channelName;
      String punishments = String.join(",", Collections.map(Collections.list(activePunishments), p -> p.rank.name.toString()));
      this.logService.logDebug(this, String.format("Ignoring chat message from user '%s' because of the following active punishments: %s", name, punishments));
      return;
    }

    try {
      Integer lvl = item.author.levelInfo.level;
      IChatComponent level = styledText(lvl.toString(), getLevelStyle(lvl));
      IChatComponent rank = new ViewerTagComponent(this.config, item.platform);
      IChatComponent player = this.messageService.getUserComponent(item.author);
      McChatResult mcChatResult = this.ytChatToMcChat(item, this.minecraftProxyService.getChatFontRenderer());

      IChatComponent joinedMessage = joinComponents("", mcChatResult.chatComponents);
      joinedMessage = this.messageService.ensureNonempty(joinedMessage, "Empty message...");

      ArrayList<IChatComponent> components = new ArrayList<>();
      components.add(level);
      components.add(rank);
      components.add(player);
      components.add(joinedMessage);
      IChatComponent message = joinComponents(" ", components);

      this.minecraftProxyService.printChatMessage("YouTube chat", message);
      if (mcChatResult.includesMention) {
        this.soundService.playDing();
      }
    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print %s chat message with id '%s': %s", item.platform, item.id, e.getMessage()));
    }
  }

  public LevelUpEventData.Out onLevelUp(LevelUpEventData.In in) {
    if (in.newLevel == 0 || in.newLevel % 5 != 0) {
      return new LevelUpEventData.Out();
    }

    try {
      IChatComponent message;
      if (in.newLevel % 20 == 0) {
        message = this.messageService.getLargeLevelUpMessage(in.user, in.newLevel);
        this.soundService.playLevelUp(1 - in.newLevel / 200.0f);
      } else {
        message = this.messageService.getSmallLevelUpMessage(in.user, in.newLevel);
        this.soundService.playLevelUp(2);
      }

      this.minecraftProxyService.printChatMessage("Level up", message);
    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print level up message for '%s': %s", in.user, e.getMessage()));
    }

    return new LevelUpEventData.Out();
  }

  public NewTwitchFollowerEventData.Out onNewTwitchFollower(NewTwitchFollowerEventData.In in) {
    this.soundService.playLevelUp(1.75f);

    try {
      IChatComponent message = this.messageService.getNewFollowerMessage(in.displayName);
      this.minecraftProxyService.printChatMessage("New follower", message);

    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print new follower message for '%s'", in.displayName));
    }

    return new NewTwitchFollowerEventData.Out();
  }

  public void printLeaderboard(PublicRankedUser[] users, @Nullable Integer highlightIndex) {
    if (users.length == 0) {
      this.minecraftProxyService.printChatMessage("Leaderboard", this.messageService.getInfoMessage("No entries to show."));
      return;
    }

    PublicRankedUser highlightUser = highlightIndex == null ? null : users[highlightIndex];
    LeaderboardRenderer renderer = new LeaderboardRenderer(this.messageService, highlightUser);
    ChatPagination<PublicRankedUser> pagination = new ChatPagination<>(this.logService, this.minecraftProxyService, this.messageService, renderer, users, 10, "Experience Leaderboard");
    pagination.render();
  }

  public void printUserList(PublicUserNames[] users) {
    if (users.length == 0) {
      this.minecraftProxyService.printChatMessage("UserList", this.messageService.getInfoMessage("No items to show."));
      return;
    }

    UserNameRenderer renderer = new UserNameRenderer(this.messageService);
    ChatPagination<PublicUserNames> pagination = new ChatPagination<>(this.logService, this.minecraftProxyService, this.messageService, renderer, users, 10, "Search Results");
    pagination.render();
  }

  public void printError(Throwable e) {
    String msg = getApiErrorMessage(e);
    IChatComponent message = this.messageService.getErrorMessage(msg);
    this.minecraftProxyService.printChatMessage("Error", message);
  }

  public void printInfo(String message) {
    IChatComponent info = this.messageService.getInfoMessage(message);
    this.minecraftProxyService.printChatMessage("Info", info);
  }

  private McChatResult ytChatToMcChat(PublicChatItem item, FontRenderer fontRenderer) throws Exception {
    ArrayList<IChatComponent> components = new ArrayList<>();
    boolean includesMention = false;

    @Nullable MessagePartType prevType = null;
    @Nullable String prevText = null;
    for (PublicMessagePart msg: item.messageParts) {
      String text;
      ChatStyle style;
      if (msg.type == MessagePartType.text) {
        assert msg.textData != null;
        text = this.filterService.censorNaughtyWords(msg.textData.text);
        style = YT_CHAT_MESSAGE_TEXT_STYLE.setBold(msg.textData.isBold).setItalic(msg.textData.isItalics);

      } else if (msg.type == MessagePartType.emoji) {
        assert msg.emojiData != null;
        String name = msg.emojiData.name;
        char[] nameChars = name.toCharArray();

        // the name could be the literal emoji unicode character
        // check if the resource pack supports it - if so, use it!
        if (nameChars.length == 1 && fontRenderer.getCharWidth(nameChars[0]) > 0) {
          text = name;
          style = YT_CHAT_MESSAGE_TEXT_STYLE;
        } else {
          text = msg.emojiData.label; // e.g. :slightly_smiling:
          style = YT_CHAT_MESSAGE_EMOJI_STYLE;
        }

      } else if (msg.type == MessagePartType.customEmoji) {
        prevType = msg.type;
        prevText = null;

        assert msg.customEmojiData != null;
        components.add(new ImageChatComponent(() -> this.imageService.createTexture(msg.customEmojiData.customEmoji.imageData)));
        continue;

      } else if (msg.type == MessagePartType.cheer) {
        assert msg.cheerData != null;
        text = String.format("[cheer with amount %d]", msg.cheerData.amount);
        style = YT_CHAT_MESSAGE_CHEER_STYLE;

      } else throw new Exception("Invalid partial message type " + msg.type);

      // add space between components except when we have two text types after one another.
      if (prevType != null && !(msg.type == MessagePartType.text && prevType == MessagePartType.text)) {
        if (text.startsWith(" ") || (prevText != null && prevText.endsWith(" "))) {
          // already spaced out
        } else {
          text = " " + text;
        }
      }

      if (msg.type == MessagePartType.text) {
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

  private static class McChatResult {
    public final List<IChatComponent> chatComponents;
    public final boolean includesMention;

    private McChatResult(List<IChatComponent> chatComponents, boolean includesMention) {
      this.chatComponents = chatComponents;
      this.includesMention = includesMention;
    }
  }
}
