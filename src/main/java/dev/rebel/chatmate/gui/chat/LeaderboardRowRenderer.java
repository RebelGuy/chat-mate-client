package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRowRenderer;
import dev.rebel.chatmate.gui.chat.ChatPagination.PartiallyVisibleItem;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.api.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardRowRenderer extends PaginationRowRenderer<PublicRankedUser> {
  private final DimFactory dimFactory;
  private final MessageService messageService;
  private final @Nullable PublicRankedUser highlightUser;

  public LeaderboardRowRenderer(DimFactory dimFactory, MessageService messageService, @Nullable PublicRankedUser highlightUser) {
    this.dimFactory = dimFactory;
    this.messageService = messageService;
    this.highlightUser = highlightUser;
  }

  @Override
  public int getItemRows(PublicRankedUser item) {
    return 1;
  }

  @Override
  public IChatComponent renderItem(PublicRankedUser item, int subIndex, List<PartiallyVisibleItem<PublicRankedUser>> allPartialItemsOnPage, FontEngine fontEngine, Dim chatWidth, Dim effectiveChatWidth) {
    List<PublicRankedUser> allItemsOnPage = Collections.map(Collections.list(allPartialItemsOnPage), x -> x.item);
    int rankDigits = String.valueOf(allItemsOnPage.get(allItemsOnPage.size() - 1).rank).length();
    int levelDigits = String.valueOf(allItemsOnPage.get(0).user.levelInfo.level + 1).length();
    List<Dim> allNameWidths = Collections.map(allItemsOnPage, entry -> fontEngine.getStringWidthDim(entry.user.channelInfo.channelName));
    Dim nameWidth = Dim.min(chatWidth.minus(this.dimFactory.fromGui(5)).over(3), Dim.max(allNameWidths));

    boolean anyHighlighting = highlightUser != null;
    boolean deEmphasise = anyHighlighting && this.highlightUser != item;

    return this.messageService.getRankedEntryMessage(item, deEmphasise, rankDigits, levelDigits, nameWidth, effectiveChatWidth);
  }
}
