package dev.rebel.chatmate.services;

import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.models.chat.GetChatResponse.PartialChatMessage;
import dev.rebel.chatmate.models.chat.PartialChatMessageType;
import dev.rebel.chatmate.models.experience.RankedEntry;
import dev.rebel.chatmate.services.events.ChatMateEventService;
import dev.rebel.chatmate.services.events.models.LevelUpEventData;
import dev.rebel.chatmate.services.util.Action3;
import dev.rebel.chatmate.services.util.Action4;
import dev.rebel.chatmate.services.util.TextHelpers;
import dev.rebel.chatmate.services.util.TextHelpers.StringMask;
import dev.rebel.chatmate.services.util.TextHelpers.WordFilter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.*;
import org.apache.commons.lang3.ArrayUtils;
import scala.Function3;
import scala.Product3;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.rebel.chatmate.models.Styles.*;
import static dev.rebel.chatmate.services.util.ChatHelpers.joinComponents;
import static dev.rebel.chatmate.services.util.ChatHelpers.styledTextWithMask;

public class McChatService {
  private final MinecraftProxyService minecraftProxyService;
  private final LogService logService;
  private final FilterService filterService;
  private final SoundService soundService;
  private final ChatMateEventService chatMateEventService;
  private final MessageService messageService;

  public McChatService(MinecraftProxyService minecraftProxyService,
                       LogService logService,
                       FilterService filterService,
                       SoundService soundService,
                       ChatMateEventService chatMateEventService,
                       MessageService messageService) {
    this.minecraftProxyService = minecraftProxyService;
    this.logService = logService;
    this.filterService = filterService;
    this.soundService = soundService;
    this.chatMateEventService = chatMateEventService;
    this.messageService = messageService;

    this.chatMateEventService.onLevelUp(this::onLevelUp, null);
  }

  public void printStreamChatItem(ChatItem item) {
    if (!this.minecraftProxyService.canPrintChatMessage()) {
      return;
    }

    try {
      Integer lvl = item.author.level;
      IChatComponent level = styledText(lvl.toString(), getLevelStyle(lvl));
      IChatComponent rank = styledText("VIEWER", VIEWER_RANK_STYLE);
      IChatComponent player = styledText(item.author.name, VIEWER_NAME_STYLE);
      McChatResult mcChatResult = this.ytChatToMcChat(item, this.minecraftProxyService.getChatFontRenderer());

      ArrayList<IChatComponent> components = new ArrayList<>();
      components.add(level);
      components.add(rank);
      components.add(player);
      components.add(joinComponents("", mcChatResult.chatComponents));
      IChatComponent message = joinComponents(" ", components);

      this.minecraftProxyService.printChatMessage("YouTube chat", message);
      if (mcChatResult.includesMention) {
        this.soundService.playDing();
      }
    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print YouTube chat message with id '%s': %s", item.id, e.getMessage()));
    }
  }

  public LevelUpEventData.Out onLevelUp(LevelUpEventData.In in) {
    if (!this.minecraftProxyService.canPrintChatMessage() || in.newLevel == 0 || in.newLevel % 5 != 0) {
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

      this.minecraftProxyService.printChatMessage("Level up", message);
    } catch (Exception e) {
      this.logService.logError(this, String.format("Could not print level up message for '%s': %s", in.channelName, e.getMessage()));
    }

    return new LevelUpEventData.Out();
  }

  public void printLeaderboard(RankedEntry[] entries, @Nullable Integer highlightEntry) {
    if (entries.length == 0) {
      this.minecraftProxyService.printChatMessage("Leaderboard", this.messageService.getInfoMessage("No entries to show."));
      return;
    }
    if (highlightEntry != null && (highlightEntry < 0 || highlightEntry >= entries.length)) {
      highlightEntry = null;
    }

    boolean anyHighlighting = highlightEntry != null;
    ChatLeaderboard leaderboard = new ChatLeaderboard(entries, highlightEntry, (visibleEntries, highlighted, onPrevPage, onNextPage) -> {
      int rankDigits = String.valueOf(visibleEntries[visibleEntries.length - 1].rank).length();
      int levelDigits = String.valueOf(visibleEntries[0].level + 1).length();
      List<Integer> allNameWidths = Arrays.stream(visibleEntries).map(entry -> this.minecraftProxyService.getChatFontRenderer().getStringWidth(entry.channelName)).collect(Collectors.toList());
      int nameWidth = Math.min((this.minecraftProxyService.getChatWidth() - 5) / 3, Collections.max(allNameWidths));
      int messageWidth = this.minecraftProxyService.getChatWidth();

      for (int i = 0; i < visibleEntries.length; i++) {
        RankedEntry entry = visibleEntries[i];
        boolean deEmphasise = anyHighlighting && highlighted != i;
        IChatComponent entryComponent = this.messageService.getRankedEntryMessage(entry, deEmphasise, rankDigits, levelDigits, nameWidth, messageWidth);
        this.minecraftProxyService.printChatMessage("Leaderboard", entryComponent);
      }

      IChatComponent footer = this.messageService.getLeaderboardFooterMessage(messageWidth, onPrevPage, onNextPage);
      this.minecraftProxyService.printChatMessage("Leaderboard", footer);
      this.minecraftProxyService.printChatMessage("Leaderboard", new ChatComponentText(""));
    });

    leaderboard.print();
  }

  public void printError(Throwable e) {
    String msg;
    if (e instanceof ConnectException) {
      msg = "Unable to connect.";
    } else {
      msg = "Something went wrong.";
    }

    IChatComponent message = this.messageService.getErrorMessage(msg);
    this.minecraftProxyService.printChatMessage("Error", message);
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

  private static class McChatResult {
    public final List<IChatComponent> chatComponents;
    public final boolean includesMention;

    private McChatResult(List<IChatComponent> chatComponents, boolean includesMention) {
      this.chatComponents = chatComponents;
      this.includesMention = includesMention;
    }
  }

  private static class ChatLeaderboard {
    private static final int ENTRIES_PER_PAGE = 10;

    private final RankedEntry[] underlyingEntries;
    private final @Nullable Integer highlightedEntry;
    private final Action4<RankedEntry[], Integer, Runnable, Runnable> renderer;

    private int currentPage = 0;

    public ChatLeaderboard(RankedEntry[] underlyingEntries, @Nullable Integer highlightedEntry, Action4<RankedEntry[], Integer, Runnable, Runnable> renderer) {
      this.underlyingEntries = underlyingEntries;
      this.highlightedEntry = highlightedEntry;
      this.renderer = renderer;
    }

    public void print() {
      this.renderer.run(
          this.getVisibleEntries(),
          this.getHighlightedIndexOfCurrentPage(),
          this.canGoToPreviousPage() ? this::previousPage : null,
          this.canGoToNextPage() ? this::nextPage : null
      );
    }

    public boolean canGoToPreviousPage() {
      return this.currentPage > 0;
    }

    public boolean canGoToNextPage() {
      int maxPage = this.underlyingEntries.length / ENTRIES_PER_PAGE;
      return this.currentPage < maxPage;
    }

    public void nextPage() {
      if (this.canGoToNextPage()) {
        this.currentPage++;
        this.print();
      }
    }

    public void previousPage() {
      if (this.canGoToPreviousPage()) {
        this.currentPage--;
        this.print();
      }
    }

    public RankedEntry[] getVisibleEntries() {
      if (underlyingEntries.length <= ENTRIES_PER_PAGE) {
        return Arrays.copyOf(this.underlyingEntries, this.underlyingEntries.length);
      } else {
        int from = this.getVisibleStartIndex();
        int to = this.getVisibleEndIndex();
        return Arrays.copyOfRange(this.underlyingEntries, from, to + 1);
      }
    }

    public @Nullable Integer getHighlightedIndexOfCurrentPage() {
      if (this.highlightedEntry == null) {
        return null;
      }

      int from = this.getVisibleStartIndex();
      int to = this.getVisibleEndIndex();
      int shiftedHighlighted = this.highlightedEntry - from;
      if (shiftedHighlighted >= 0 && shiftedHighlighted <= to) {
        return shiftedHighlighted;
      }

      return null;
    }

    private int getVisibleStartIndex() {
      return this.currentPage * ENTRIES_PER_PAGE;
    }

    private int getVisibleEndIndex() {
      int to = this.currentPage * ENTRIES_PER_PAGE + ENTRIES_PER_PAGE - 1;
      if (to >= this.underlyingEntries.length) {
        to = this.underlyingEntries.length - 1;
      }
      return to;
    }
  }
}
