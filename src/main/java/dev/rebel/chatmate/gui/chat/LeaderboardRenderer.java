package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRenderer;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.api.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.services.MessageService;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardRenderer extends PaginationRenderer<PublicRankedUser> {
  private final DimFactory dimFactory;
  private final MessageService messageService;
  private final @Nullable PublicRankedUser highlightUser;

  public LeaderboardRenderer(DimFactory dimFactory, MessageService messageService, @Nullable PublicRankedUser highlightUser) {
    this.dimFactory = dimFactory;
    this.messageService = messageService;
    this.highlightUser = highlightUser;
  }

  @Override
  public IChatComponent renderItem(PublicRankedUser item, PublicRankedUser[] allItemsOnPage, FontEngine fontEngine, Dim chatWidth, Dim effectiveChatWidth) {
    int rankDigits = String.valueOf(allItemsOnPage[allItemsOnPage.length - 1].rank).length();
    int levelDigits = String.valueOf(allItemsOnPage[0].user.levelInfo.level + 1).length();
    List<Dim> allNameWidths = Arrays.stream(allItemsOnPage).map(entry -> fontEngine.getStringWidthDim(entry.user.channelInfo.channelName)).collect(Collectors.toList());
    Dim nameWidth = Dim.min(chatWidth.minus(this.dimFactory.fromGui(5)).over(3), Dim.max(allNameWidths));

    boolean anyHighlighting = highlightUser != null;
    boolean deEmphasise = anyHighlighting && this.highlightUser != item;

    return this.messageService.getRankedEntryMessage(item, deEmphasise, rankDigits, levelDigits, nameWidth, effectiveChatWidth);
  }
}
