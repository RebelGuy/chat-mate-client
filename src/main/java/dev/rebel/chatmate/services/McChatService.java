package dev.rebel.chatmate.services;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatImage;
import dev.rebel.chatmate.api.publicObjects.emoji.PublicCustomEmoji;
import dev.rebel.chatmate.config.Config.CommandMessageChatVisibility;
import dev.rebel.chatmate.events.Event;
import dev.rebel.chatmate.events.models.*;
import dev.rebel.chatmate.gui.CustomGuiNewChat;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.StateManagement.State;
import dev.rebel.chatmate.gui.chat.*;
import dev.rebel.chatmate.gui.chat.UserSearchResultRowRenderer.AggregatedUserSearchResult;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart;
import dev.rebel.chatmate.api.publicObjects.chat.PublicMessagePart.MessagePartType;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.events.ChatMateChatService;
import dev.rebel.chatmate.events.ChatMateEventService;
import dev.rebel.chatmate.events.MinecraftChatEventService;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.TextHelpers;
import dev.rebel.chatmate.util.TextHelpers.StringMask;
import dev.rebel.chatmate.util.TextHelpers.WordFilter;
import net.minecraft.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static dev.rebel.chatmate.gui.chat.Styles.*;
import static dev.rebel.chatmate.api.proxy.EndpointProxy.getApiErrorMessage;
import static dev.rebel.chatmate.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.util.ChatHelpers.styledTextWithMask;
import static dev.rebel.chatmate.util.Objects.*;

public class McChatService {
  private final MinecraftProxyService minecraftProxyService;
  private final LogService logService;
  private final FilterService filterService;
  private final SoundService soundService;
  private final ChatMateEventService chatMateEventService;
  private final MessageService messageService;
  private final ImageService imageService;
  private final Config config;
  private final FontEngine fontEngine;
  private final DimFactory dimFactory;
  private final CustomGuiNewChat customGuiNewChat;
  private final MinecraftChatEventService minecraftChatEventService;
  private final InteractiveContext interactiveContext;

  public McChatService(MinecraftProxyService minecraftProxyService,
                       LogService logService,
                       FilterService filterService,
                       SoundService soundService,
                       ChatMateEventService chatMateEventService,
                       MessageService messageService,
                       ImageService imageService,
                       Config config,
                       ChatMateChatService chatMateChatService,
                       FontEngine fontEngine,
                       DimFactory dimFactory,
                       CustomGuiNewChat customGuiNewChat,
                       MinecraftChatEventService minecraftChatEventService,
                       InteractiveContext interactiveContext) {
    this.minecraftProxyService = minecraftProxyService;
    this.logService = logService;
    this.filterService = filterService;
    this.soundService = soundService;
    this.chatMateEventService = chatMateEventService;
    this.messageService = messageService;
    this.imageService = imageService;
    this.config = config;
    this.fontEngine = fontEngine;
    this.dimFactory = dimFactory;
    this.customGuiNewChat = customGuiNewChat;
    this.minecraftChatEventService = minecraftChatEventService;
    this.interactiveContext = interactiveContext;

    this.chatMateEventService.onLevelUp(this::onLevelUp);
    this.chatMateEventService.onNewTwitchFollower(this::onNewTwitchFollower);
    this.chatMateEventService.onNewViewer(this::onNewViewer);
    this.chatMateEventService.onChatMessageDeleted(this::onChatMessageDeleted);
    this.chatMateEventService.onRankUpdated(this::onRankUpdated);
    this.config.getShowChatPlatformIconEmitter().onChange(_value -> this.minecraftProxyService.refreshChat());

    chatMateChatService.onNewChat(newChat -> {
      for (PublicChatItem chat: newChat.getData().chatItems) {
        this.printStreamChatItem(chat);
      }
    }, null);
  }

  public void printStreamChatItem(PublicChatItem item) {
    boolean isCommand = item.commandId != null;
    CommandMessageChatVisibility commandMessageVisibility = this.config.getCommandMessageChatVisibilityEmitter().get();
    if (isCommand && commandMessageVisibility == CommandMessageChatVisibility.HIDDEN) {
      return;
    }

    PublicUserRank[] activePunishments = item.author.getActivePunishments();
    if (activePunishments.length > 0) {
      String name = item.author.channel.displayName;
      String punishments = String.join(",", Collections.map(Collections.list(activePunishments), p -> p.rank.name.toString()));
      this.logService.logDebug(this, String.format("Ignoring chat message from user '%s' because of the following active punishments: %s", name, punishments));
      return;
    }

    boolean greyOut = isCommand && commandMessageVisibility == CommandMessageChatVisibility.GREYED_OUT;

    try {
      Integer lvl = item.author.levelInfo.level;
      ChatStyle levelStyle = getLevelStyle(lvl);
      if (greyOut) {
        levelStyle = levelStyle.setColor(EnumChatFormatting.DARK_GRAY);
      }
      IChatComponent level = styledText(lvl.toString(), levelStyle);

      IChatComponent platform = new PlatformViewerTagComponent(this.dimFactory, this.config, item.platform, greyOut);

      IChatComponent rank = this.messageService.getRankComponent(Collections.list(item.author.activeRanks));
      if (greyOut) {
        rank = rank.setChatStyle(rank.getChatStyle().setColor(EnumChatFormatting.DARK_GRAY));
      }

      Font viewerNameFont = VIEWER_NAME_FONT.create(this.dimFactory);
      if (greyOut) {
        viewerNameFont = viewerNameFont.withColour(Colour.GREY33);
      }
      IChatComponent player = this.messageService.getUserComponent(item.author, viewerNameFont, item.author.channel.displayName, true, true, false, false);

      McChatResult mcChatResult = this.streamChatToMcChat(item, this.fontEngine, greyOut);
      IChatComponent joinedMessage = joinComponents("", mcChatResult.chatComponents);
      joinedMessage = this.messageService.ensureNonempty(joinedMessage, "Empty message...");

      ArrayList<IChatComponent> components = new ArrayList<>();
      components.add(level);
      components.add(platform);
      components.add(rank);
      components.add(player);
      components.add(joinedMessage);
      if (isCommand) {
        components.add(new ChatCommandChatComponent(this.interactiveContext, item.commandId));
      }

      // if you change the structure of the chat message component, ensure deleteStreamChatItem() still works
      IChatComponent message = joinComponents(" ", components, c -> c == platform);
      ContainerChatComponent component = new ContainerChatComponent(message, item);

      this.minecraftProxyService.printChatMessage("YouTube chat", component);
      if (mcChatResult.includesMention) {
        this.soundService.playDing();
      }
    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print %s chat message with id '%s': %s", item.platform, item.id, e.getMessage()), e);
    }
  }

  public void onLevelUp(Event<LevelUpEventData> event) {
    LevelUpEventData data = event.getData();
    if (data.newLevel == 0 || data.newLevel % 5 != 0) {
      return;
    }

    try {
      IChatComponent message;
      if (data.newLevel % 20 == 0) {
        message = this.messageService.getLargeLevelUpMessage(data.user, data.newLevel);
        this.soundService.playLevelUp(1 - data.newLevel / 200.0f);
      } else {
        message = this.messageService.getSmallLevelUpMessage(data.user, data.newLevel);
        this.soundService.playLevelUp(2);
      }

      this.minecraftProxyService.printChatMessage("Level up", message);
    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print level up message for '%s': %s", data.user, e.getMessage()));
    }
  }

  public void onNewTwitchFollower(Event<NewTwitchFollowerEventData> event) {
    this.soundService.playLevelUp(1.75f);

    NewTwitchFollowerEventData data = event.getData();

    try {
      IChatComponent message = this.messageService.getNewFollowerMessage(data.displayName);
      this.minecraftProxyService.printChatMessage("New follower", message);

    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print new follower message for '%s'", data.displayName));
    }
  }

  public void onNewViewer(Event<NewViewerEventData> event) {
    this.soundService.playLevelUp(1.75f);

    NewViewerEventData data = event.getData();

    try {
      IChatComponent message = this.messageService.getNewViewerMessage(data.newViewer.user);
      this.minecraftProxyService.printChatMessage("New viewer", message);

    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print new viewer message for '%s'", data.newViewer.user.channel.displayName));
    }
  }

  public void onChatMessageDeleted(Event<ChatMessageDeletedEventData> event) {
    ChatMessageDeletedEventData data = event.getData();
    int deletedMessageId = data.chatMessageDeletedData.chatMessageId;

    Predicate<IChatComponent> predicate = oldComponent -> {
      Boolean match = casted(ContainerChatComponent.class, oldComponent, containerComponent ->
          casted(PublicChatItem.class, containerComponent.getData(), chatItem ->
              chatItem.id == deletedMessageId
          )
      );

      return match != null && match;
    };
    UnaryOperator<IChatComponent> componentGenerator = oldComponent -> {
      // the structure of chat messages is known. we want to retain all sibling components up to the user's name, and replace everything after (the message part)
      ContainerChatComponent containerChatComponent = (ContainerChatComponent)oldComponent;
      ChatComponentText innerComponent = (ChatComponentText)containerChatComponent.getComponent();

      IChatComponent newComponent = innerComponent.getSiblings().get(0);
      State<Integer> skipIndexAfter = new State<>(innerComponent.getSiblings().size());
      for (int i = 1; i < innerComponent.getSiblings().size(); i++) {
        if (i > skipIndexAfter.getState()) {
          continue;
        }

        IChatComponent sibling = innerComponent.getSiblings().get(i);
        newComponent.appendSibling(sibling);

        State<Integer> index = new State<>(i);
        castedVoid(ContainerChatComponent.class, sibling, container -> {
          castedVoid(UserNameChatComponent.class, container.getComponent(), c -> {
            // +1 to append the padding component before the message component
            skipIndexAfter.setState(index.getState() + 1);
          });
        });
      }
      newComponent.appendSibling(styledText("Message deleted.", INFO_SUBTLE_MSG_STYLE.get()));

      return newComponent;
    };

    this.minecraftProxyService.replaceComponentInChat(predicate, componentGenerator);
  }

  public void onRankUpdated(Event<RankUpdatedEventData> event) {
    this.soundService.playLevelUp(1.75f);

    RankUpdatedEventData data = event.getData();

    try {
      IChatComponent message = this.messageService.getRankUpdatedMessage(data.rankName, data.isAdded, data.user, data.platformRanks);
      this.minecraftProxyService.printChatMessage("Rank updated", message);

    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print rank updated message for '%s'", data.user.channel.displayName));
    }
  }

  public void printLeaderboard(PublicRankedUser[] users, @Nullable Integer highlightIndex) {
    if (users.length == 0) {
      this.minecraftProxyService.printChatMessage("Leaderboard", this.messageService.getInfoMessage("No entries to show."));
      return;
    }

    PublicRankedUser highlightUser = highlightIndex == null ? null : users[highlightIndex];
    LeaderboardRowRenderer renderer = new LeaderboardRowRenderer(this.dimFactory, this.messageService, highlightUser);
    ChatPagination<PublicRankedUser> pagination = new ChatPagination<>(this.logService, this.minecraftProxyService, this.customGuiNewChat, this.dimFactory, this.messageService, this.minecraftChatEventService, this.fontEngine, renderer, Collections.list(users), 10, "Experience Leaderboard");
    pagination.render();
  }

  public void printUserSearchResults(List<AggregatedUserSearchResult> results) {
    if (results.size() == 0) {
      this.minecraftProxyService.printChatMessage("UserList", this.messageService.getInfoMessage("No items to show."));
      return;
    }

    UserSearchResultRowRenderer renderer = new UserSearchResultRowRenderer(this.messageService);
    ChatPagination<AggregatedUserSearchResult> pagination = new ChatPagination<>(this.logService, this.minecraftProxyService, this.customGuiNewChat, this.dimFactory, this.messageService, this.minecraftChatEventService, this.fontEngine, renderer, results, 10, "Search Results");
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

  private McChatResult streamChatToMcChat(PublicChatItem item, FontEngine fontEngine, boolean greyOut) throws Exception {
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
        style = YT_CHAT_MESSAGE_TEXT_STYLE.get().setBold(msg.textData.isBold).setItalic(msg.textData.isItalics);

      } else if (msg.type == MessagePartType.emoji) {
        assert msg.emojiData != null;

        if (msg.emojiData.image != null) {
          // render the emoji
          PublicChatImage image = msg.emojiData.image;
          ImageChatComponent imageChatComponent = new ImageChatComponent(
              // if we don't have dimensions (e.g. svg), we assume a square image. this is true in most cases.
              // if our guess is wrong, we can expect some slight re-shifting of items in chat, which is acceptable
              this.imageService.createCacheableTextureFromUrl(image.width, image.height, image.url, msg.emojiData.getCacheKey()),
              this.dimFactory.fromGui(1),
              this.dimFactory.fromGui(1),
              greyOut
          );
          components.add(imageChatComponent);
          continue;
        } else {
          // draw the name/label of the emoji
          String name = msg.emojiData.name;
          char[] nameChars = name.toCharArray();

          // the name could be the literal emoji unicode character
          // check if the resource pack supports it - if so, use it!
          if (nameChars.length == 1 && fontEngine.getCharWidth(nameChars[0]).getGui() > 0) {
            text = name;
            style = YT_CHAT_MESSAGE_TEXT_STYLE.get();
          } else {
            text = msg.emojiData.label; // e.g. :slightly_smiling:
            style = YT_CHAT_MESSAGE_EMOJI_STYLE.get();
          }
        }

      } else if (msg.type == MessagePartType.customEmoji) {
        prevType = msg.type;
        prevText = null;

        assert msg.customEmojiData != null;
        PublicCustomEmoji customEmoji = msg.customEmojiData.customEmoji;
        ImageChatComponent imageChatComponent = new ImageChatComponent(
            this.imageService.createCacheableTextureFromUrl(customEmoji.imageWidth, customEmoji.imageHeight, customEmoji.imageUrl, customEmoji.getCacheKey()),
            this.dimFactory.fromGui(1),
            this.dimFactory.fromGui(1),
            greyOut
        );
        components.add(imageChatComponent);
        continue;

      } else if (msg.type == MessagePartType.cheer) {
        assert msg.cheerData != null;
        text = String.format("[cheer with amount %d]", msg.cheerData.amount);
        style = YT_CHAT_MESSAGE_CHEER_STYLE.get();

      } else throw new Exception("Invalid partial message type " + msg.type);

      // add space between components except when we have two text types after one another.
      if (prevType != null && prevType != MessagePartType.customEmoji && !(msg.type == MessagePartType.text && prevType == MessagePartType.text)) {
        if (text.startsWith(" ") || (prevText != null && prevText.endsWith(" "))) {
          // already spaced out
        } else {
          text = " " + text;
        }
      }

      if (greyOut) {
        style = style.setColor(EnumChatFormatting.DARK_GRAY);
      }

      if (msg.type == MessagePartType.text) {
        String[] stringArray = Collections.toArray(this.config.getChatMentionFilter().get(), new String[0]); // i'm feeling sick 🤮🤮🤮
        WordFilter[] mentionFilter = TextHelpers.makeWordFilters(stringArray);
        StringMask mentionMask = FilterService.filterWords(text, mentionFilter);

        ChatStyle mentionStyle = MENTION_TEXT_STYLE.get();
        if (greyOut) {
          mentionStyle = mentionStyle.setColor(EnumChatFormatting.DARK_GRAY);
        }
        components.addAll(styledTextWithMask(text, style, mentionMask, mentionStyle));

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
