package dev.rebel.chatmate.services;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ContainerChatComponent;
import dev.rebel.chatmate.gui.chat.ImageChatComponent;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText.PrecisionAlignment;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText.PrecisionLayout;
import dev.rebel.chatmate.gui.chat.PrecisionChatComponentText.PrecisionValue;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.models.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.models.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.services.util.ChatHelpers.ClickEventWithCallback;
import dev.rebel.chatmate.services.util.EnumHelpers;
import dev.rebel.chatmate.services.util.TextHelpers;
import dev.rebel.chatmate.services.util.TextHelpers.ExtractedFormatting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static dev.rebel.chatmate.models.Styles.*;
import static dev.rebel.chatmate.services.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.services.util.ChatHelpers.stringWithWidth;

public class MessageService {
  private static final IChatComponent INFO_PREFIX = styledText("ChatMate>", INFO_MSG_PREFIX_STYLE);

  private final Random random = new Random();
  private final LogService logService;
  private final FontEngine fontEngine;

  public MessageService(LogService logService, FontEngine fontEngine) {
    this.logService = logService;
    this.fontEngine = fontEngine;
  }

  public IChatComponent getErrorMessage(String msg) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(msg, ERROR_MSG_STYLE));
    return joinComponents(" ", list);
  }

  public IChatComponent getInfoMessage(String msg) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(msg, INFO_MSG_STYLE));
    return joinComponents(" ", list);
  }

  public IChatComponent getSmallLevelUpMessage(PublicUser user, int newLevel) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(" ", INFO_MSG_STYLE));
    list.add(this.getUserComponent(user));
    list.add(styledText(" has just reached level " + newLevel + "!", INFO_MSG_STYLE));
    return joinComponents("", list);
  }

  public IChatComponent getLargeLevelUpMessage(PublicUser user, int newLevel) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(this.getLargeLevelUpIntro(user, newLevel));
    list.add(this.getLargeLevelUpBody(user, newLevel));
    list.add(this.getLargeLevelUpOutro(user, newLevel));
    return joinComponents(" ", list);
  }

  public IChatComponent getRankedEntryMessage(PublicRankedUser entry, boolean deEmphasise, int rankDigits, int levelDigits, int nameWidth, int messageWidth) {
    // example:
    // #24 ShiroTheS... 41 |⣿⣿⣿⣿⣿     | 42
    // rank name levelStart barStart barFilled barBlank barEnd levelEnd

    int padding = 4;

    int rankNumberWidth = this.fontEngine.getStringWidth("#" + String.join("", Collections.nCopies(rankDigits, "4")));
    PrecisionLayout rankLayout = new PrecisionLayout(new PrecisionValue(0), new PrecisionValue(rankNumberWidth), PrecisionAlignment.RIGHT);
    String rankText = "#" + entry.rank;
    int x = rankNumberWidth + padding;

    PrecisionLayout nameLayout = new PrecisionLayout(new PrecisionValue(x), new PrecisionValue(nameWidth), PrecisionAlignment.LEFT);
    x += nameWidth + padding;

    int levelNumberWidth = this.fontEngine.getStringWidth(String.join("", Collections.nCopies(levelDigits, "4")));
    PrecisionLayout levelStartLayout = new PrecisionLayout(new PrecisionValue(x), new PrecisionValue(levelNumberWidth), PrecisionAlignment.CENTRE);
    String levelStart = String.valueOf(entry.user.levelInfo.level);
    x += levelNumberWidth + padding;

    String barStart = "|";
    int barStartWidth = this.fontEngine.getStringWidth(barStart);
    PrecisionLayout barStartLayout = new PrecisionLayout(new PrecisionValue(x), new PrecisionValue(barStartWidth), PrecisionAlignment.RIGHT);
    x += barStartWidth; // no padding

    String barEnd = "|";
    int barEndWidth = this.fontEngine.getStringWidth(barStart) + padding;

    int barBodyWidth = messageWidth - x - barEndWidth - levelNumberWidth;
    int fillWidth = Math.round(barBodyWidth * entry.user.levelInfo.levelProgress);
    String filledBar = stringWithWidth(this.fontEngine, "", "", '⣿', fillWidth) + "⣿";
    PrecisionLayout filledBarLayout = new PrecisionLayout(new PrecisionValue(x), new PrecisionValue(fillWidth), PrecisionAlignment.LEFT, "");
    x += fillWidth; // no padding

    int emptyBarWidth = barBodyWidth - fillWidth;
    String emptyBar = "";
    PrecisionLayout emptyBarLayout = new PrecisionLayout(new PrecisionValue(x), new PrecisionValue(emptyBarWidth), PrecisionAlignment.LEFT);
    x += emptyBarWidth; // no padding

    PrecisionLayout barEndLayout = new PrecisionLayout(new PrecisionValue(x), new PrecisionValue(barEndWidth), PrecisionAlignment.LEFT);
    x += barEndWidth; // padding already included

    PrecisionLayout levelEndLayout = new PrecisionLayout(new PrecisionValue(x), new PrecisionValue(levelNumberWidth), PrecisionAlignment.CENTRE);
    String levelEnd = String.valueOf(entry.user.levelInfo.level + 1);

    List<Tuple2<PrecisionLayout, IChatComponent>> list = new ArrayList<>();
    list.add(new Tuple2<>(rankLayout, styledText(rankText, deEmphasise ? INFO_MSG_STYLE : GOOD_MSG_STYLE)));
    list.add(new Tuple2<>(nameLayout, this.getUserComponent(entry.user, deEmphasise ? INFO_MSG_STYLE : VIEWER_NAME_STYLE, entry.user.userInfo.channelName, true)));
    list.add(new Tuple2<>(levelStartLayout, styledText(levelStart, deEmphasise ? INFO_MSG_STYLE : getLevelStyle(entry.user.levelInfo.level))));
    list.add(new Tuple2<>(barStartLayout, styledText(barStart, INFO_MSG_STYLE)));
    list.add(new Tuple2<>(filledBarLayout, styledText(filledBar, INFO_MSG_STYLE)));
    list.add(new Tuple2<>(emptyBarLayout, styledText(emptyBar, INFO_MSG_STYLE)));
    list.add(new Tuple2<>(barEndLayout, styledText(barEnd, INFO_MSG_STYLE)));
    list.add(new Tuple2<>(levelEndLayout, styledText(levelEnd, deEmphasise ? INFO_MSG_STYLE : getLevelStyle(entry.user.levelInfo.level + 1))));
    return new PrecisionChatComponentText(list);
  }

  public IChatComponent getChannelNamesMessage(PublicUserNames userNames, int messageWidth) {
    // since different users may share the same channel name, it is helpful to also show each user's current level
    String level = String.valueOf(userNames.user.levelInfo.level);
    int levelNumberWidth = this.fontEngine.getStringWidth("444");
    PrecisionLayout levelLayout = new PrecisionLayout(new PrecisionValue(4), new PrecisionValue(levelNumberWidth), PrecisionAlignment.RIGHT);

    // todo CHAT-270: at the moment we are only showing the default channel name, but in the future it is possible that a single user
    // has multiple channels so then we must print a list
    PrecisionLayout nameLayout = new PrecisionLayout(new PrecisionValue(4 + levelNumberWidth + 4), new PrecisionValue(messageWidth), PrecisionAlignment.LEFT);
    ChatStyle style = userNames.youtubeChannelNames.length > 0 ? YOUTUBE_CHANNEL_STYLE : TWITCH_CHANNEL_STYLE;
    IChatComponent component = this.getUserComponent(userNames.user, style, userNames.user.userInfo.channelName, true);

    List<Tuple2<PrecisionLayout, IChatComponent>> list = new ArrayList<>();
    list.add(new Tuple2<>(levelLayout, styledText(level, getLevelStyle(userNames.user.levelInfo.level))));
    list.add(new Tuple2<>(nameLayout, component));
    return new PrecisionChatComponentText(list);
  }

  public IChatComponent getPaginationFooterMessage(int messageWidth, int currentPage, int maxPage, @Nullable Runnable onPrevPage, @Nullable Runnable onNextPage) {
    if (onPrevPage == null && onNextPage == null) {
      IChatComponent footer = styledText(stringWithWidth(this.fontEngine, "", "", '-', messageWidth), INFO_MSG_STYLE);
      PrecisionLayout footerLayout = new PrecisionLayout(new PrecisionValue(0), new PrecisionValue(messageWidth), PrecisionAlignment.CENTRE);
      return new PrecisionChatComponentText(Arrays.asList(new Tuple2<>(footerLayout, footer)));
    }

    String padding = "  ";
    int paddingWidth = this.fontEngine.getStringWidth(padding);

    String shortPageString = String.valueOf(currentPage);
    int shortPageStringWidth = this.fontEngine.getStringWidth(shortPageString);

    String longPageString = String.format("%d of %d", currentPage, maxPage);
    int longPageStringWidth = this.fontEngine.getStringWidth(longPageString);

    // we use layouts for the buttons because we want them to be fixed, even if the footer contents change sizes slightly
    // (e.g. the page number width could change - we don't want the buttons to slightly shift their positions as a result)
    String prevPageMsg = "<< Previous";
    int prevPageMsgWidth = this.fontEngine.getStringWidth(prevPageMsg);
    PrecisionLayout prevPageLayout = new PrecisionLayout(new PrecisionValue(paddingWidth), new PrecisionValue(messageWidth - paddingWidth), PrecisionAlignment.LEFT);

    String nextPageMsg = "Next >>";
    int nextPageMsgWidth = this.fontEngine.getStringWidth(nextPageMsg);
    PrecisionLayout nextPageLayout = new PrecisionLayout(new PrecisionValue(0), new PrecisionValue(messageWidth - paddingWidth), PrecisionAlignment.RIGHT);

    // we now have to decide what we want to render in the centre of the footer. this is how much free room we have:
    int interiorWidth = messageWidth - (paddingWidth + prevPageMsgWidth + paddingWidth + paddingWidth + nextPageMsgWidth + paddingWidth);

    String interior;
    if (interiorWidth < shortPageStringWidth) {
      // fill with `-`
      interior = stringWithWidth(this.fontEngine, "", "", '-', interiorWidth);
    } else if (interiorWidth < longPageStringWidth) {
      interior = shortPageString;
    } else {
      interior = longPageString;
    }

    // now try to also add some `-` fills to the left and right of the interior, if there is enough room.
    int availableToFillSides = (interiorWidth - (this.fontEngine.getStringWidth(interior) + paddingWidth + paddingWidth)) / 2;
    if (availableToFillSides >= this.fontEngine.getStringWidth("-")) {
      String interiorSides = stringWithWidth(this.fontEngine, "", "", '-', availableToFillSides);
      interior = interiorSides + padding + interior + padding + interiorSides;
    }
    // centre the interior exactly between the outer buttons
    PrecisionLayout interiorLayout = new PrecisionLayout(new PrecisionValue(prevPageMsgWidth), new PrecisionValue(messageWidth - prevPageMsgWidth - nextPageMsgWidth), PrecisionAlignment.CENTRE);

    ClickEventWithCallback onPrevClick = new ClickEventWithCallback(this.logService, onPrevPage, true);
    ClickEventWithCallback onNextClick = new ClickEventWithCallback(this.logService, onNextPage, true);

    ChatComponentText prevComponent = styledText(prevPageMsg, onPrevClick.bind(onPrevPage == null ? INTERACTIVE_STYLE_DISABLED.get() : INTERACTIVE_STYLE.get()));
    ChatComponentText interiorComponent = styledText(interior, INFO_MSG_STYLE);
    ChatComponentText nextComponent = styledText(nextPageMsg, onNextClick.bind(onNextPage == null ? INTERACTIVE_STYLE_DISABLED.get() : INTERACTIVE_STYLE.get()));
    return new PrecisionChatComponentText(Arrays.asList(
        new Tuple2<>(prevPageLayout, prevComponent),
        new Tuple2<>(interiorLayout, interiorComponent),
        new Tuple2<>(nextPageLayout, nextComponent)
    ));
  }

  public IChatComponent getNewFollowerMessage(String displayName) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(INFO_PREFIX);
    list.add(styledText(displayName, VIEWER_NAME_STYLE));
    list.add(styledText("has just followed on Twitch!", INFO_MSG_STYLE));
    return joinComponents(" ", list);
  }

  private IChatComponent getLargeLevelUpIntro(PublicUser user, int newLevel) {
    return pickRandom(INFO_MSG_STYLE,
        "My little rebels... I have news for you.",
        "You won't believe what just happened.",
        "Unbelievable!",
        "And they said it couldn't be done.",
        "Wow!",
        "Would you look at that.",
        "Also in the news:"
      );
  }

  private IChatComponent getLargeLevelUpBody(PublicUser user, int newLevel) {
    List<IChatComponent> list = new ArrayList<>();
    list.add(this.getUserComponent(user));
    list.add(styledText(" has reached level ", INFO_MSG_STYLE));
    list.add(styledText(String.valueOf(newLevel), getLevelStyle(newLevel)));
    list.add(styledText("!", INFO_MSG_STYLE));
    return joinComponents("", list);
  }

  private IChatComponent getLargeLevelUpOutro(PublicUser user, int newLevel) {
    return pickRandom(INFO_MSG_STYLE,
        "Let's celebrate this incredible achievement!",
        "No one would believe it if it wasn't livestreamed!",
        "We respect you.",
        "When level 100?",
        "At this point we can just quit. We have reached the peak. It can't get better than this.",
        "Now maybe it's time to go outside for a bit?",
        "I can't even count that high... " + String.join(", ", IntStream.range(1, newLevel).mapToObj(String::valueOf).toArray(String[]::new)) + "... uhh I give up.",
        "A true rebel among us.",
        "Level 100 is within reach!",
        "Level " + (newLevel + 1) + " is within reach!",
        "Congratulations!",
        styledText("Say 123 if you respect ", INFO_MSG_STYLE).appendSibling(this.getUserComponent(user)).appendSibling(styledText(".", INFO_MSG_STYLE)),
        styledText("Subscribe to ", INFO_MSG_STYLE).appendSibling(this.getUserComponent(user)).appendSibling(styledText((user.userInfo.channelName.endsWith("s") ? "'" : "'s") + " YouTube channel for daily let's play videos!", INFO_MSG_STYLE))
      );
  }

  public IChatComponent getUserComponent(PublicUser user) {
    return this.getUserComponent(user, VIEWER_NAME_STYLE, user.userInfo.channelName, true);
  }

  public IChatComponent getUserComponent(PublicUser user, ChatStyle style, String channelName, boolean showPunishmentPrefix) {
    ExtractedFormatting extractedFormatting = TextHelpers.extractFormatting(channelName);
    String unstyledName = extractedFormatting.unformattedText.trim();

    // make sure we don't try to print an empty user name
    if (this.fontEngine.getStringWidth(unstyledName) == 0) {
      unstyledName = "User " + user.id;
    }

    if (showPunishmentPrefix) {
      for (PublicUserRank punishment : user.getActivePunishments()) {
        String prefix;
        if (punishment.rank.name == PublicRank.RankName.MUTE) {
          prefix = "♪ ";
        } else if (punishment.rank.name == PublicRank.RankName.TIMEOUT) {
          prefix = "◔ ";
        } else if (punishment.rank.name == PublicRank.RankName.BAN) {
          prefix = "☠ ";
        } else {
          throw new RuntimeException("Invalid punishment rank " + punishment.rank.name);
        }

        unstyledName = prefix + unstyledName;
      }
    }

    return new ContainerChatComponent(styledText(unstyledName, style), user);
  }

  public IChatComponent getRankComponent(List<PublicRank> activeRanks) {
    @Nullable RankName rankToShow = EnumHelpers.getFirst(
        dev.rebel.chatmate.services.util.Collections.map(activeRanks, r -> r.name),
        RankName.OWNER,
        RankName.ADMIN,
        RankName.MOD,
        RankName.MEMBER,
        RankName.SUPPORTER,
        RankName.DONATOR,
        RankName.FAMOUS
    );
    @Nullable PublicRank matchingRank = dev.rebel.chatmate.services.util.Collections.first(activeRanks, r -> r.name == rankToShow);
    String rankText = matchingRank == null ? "VIEWER" : matchingRank.displayNameNoun.toUpperCase();
    return styledText(rankText, VIEWER_RANK_STYLE);
  }

  /** Ensures the component can be displayed in chat, otherwise replaces it with the provided message. */
  public IChatComponent ensureNonempty(IChatComponent component, String msgIfEmpty) {
    StringBuilder sb = new StringBuilder();

    for (IChatComponent sibling : component) {
      if (sibling instanceof ImageChatComponent) {
        return component;
      }

      sb.append(sibling.getUnformattedTextForChat());
    }

    String text = sb.toString().trim();
    if (this.fontEngine.getStringWidth(text) == 0) {
      return styledText(msgIfEmpty, INFO_SUBTLE_MSG_STYLE);
    } else {
      return component;
    }
  }

  private IChatComponent pickRandom(ChatStyle style, Object... stringOrComponent) {
    int r = random.nextInt(stringOrComponent.length);
    Object picked = stringOrComponent[r];
    if (picked instanceof String) {
      return styledText((String)picked, style);
    } else if (picked instanceof IChatComponent) {
      return (IChatComponent)picked;
    } else {
      throw new RuntimeException("Cannot pick a random message because an input had type " + picked.getClass().getSimpleName());
    }
  }
}
