package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRenderer;
import dev.rebel.chatmate.models.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.services.MessageService;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardRenderer extends PaginationRenderer<PublicRankedUser> {
  private final MessageService messageService;
  private final @Nullable PublicRankedUser highlightUser;

  public LeaderboardRenderer(MessageService messageService, @Nullable PublicRankedUser highlightUser) {
    this.messageService = messageService;
    this.highlightUser = highlightUser;
  }

  @Override
  public IChatComponent renderItem(PublicRankedUser item, PublicRankedUser[] allItemsOnPage, FontEngine fontEngine, int chatWidth, int effectiveChatWidth) {
    int rankDigits = String.valueOf(allItemsOnPage[allItemsOnPage.length - 1].rank).length();
    int levelDigits = String.valueOf(allItemsOnPage[0].user.levelInfo.level + 1).length();
    List<Integer> allNameWidths = Arrays.stream(allItemsOnPage).map(entry -> fontEngine.getStringWidth(entry.user.userInfo.channelName)).collect(Collectors.toList());
    int nameWidth = Math.min((chatWidth - 5) / 3, Collections.max(allNameWidths));

    boolean anyHighlighting = highlightUser != null;
    boolean deEmphasise = anyHighlighting && this.highlightUser != item;

    return this.messageService.getRankedEntryMessage(item, deEmphasise, rankDigits, levelDigits, nameWidth, effectiveChatWidth);
  }
}
