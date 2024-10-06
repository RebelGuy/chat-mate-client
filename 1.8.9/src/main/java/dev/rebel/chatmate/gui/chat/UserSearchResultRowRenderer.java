package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRowRenderer;
import dev.rebel.chatmate.gui.chat.ChatPagination.PartiallyVisibleItem;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.services.MessageService;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nullable;
import java.util.List;

public class UserSearchResultRowRenderer extends PaginationRowRenderer<UserSearchResultRowRenderer.AggregatedUserSearchResult> {
  private final MessageService messageService;

  public UserSearchResultRowRenderer(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public int getItemRows(AggregatedUserSearchResult item) {
    // 1 for the header
    return item.user.registeredUser == null ? 1 : 1 + item.matchedChannels.size() + item.otherChannels.size();
  }

  @Override
  public IChatComponent renderItem(AggregatedUserSearchResult item, int subIndex, List<PartiallyVisibleItem<AggregatedUserSearchResult>> allItemsOnPage, FontEngine fontEngine, Dim chatWidth, Dim effectiveChatWidth) {
    @Nullable PublicChannel channelAtIndex;
    boolean deEmphasise;
    if (item.user.registeredUser == null) {
      channelAtIndex = item.matchedChannels.get(0);
      deEmphasise = false;
    } else if (subIndex == 0) {
      channelAtIndex = null; // header
      deEmphasise = false;
    } else if (subIndex - 1 < item.matchedChannels.size()) {
      channelAtIndex = item.matchedChannels.get(subIndex - 1);
      deEmphasise = false;
    } else {
      channelAtIndex = item.otherChannels.get(subIndex - item.matchedChannels.size() - 1);
      deEmphasise = true;
    }

    return this.messageService.getChannelSearchResultMessage(chatWidth, item.user, channelAtIndex, deEmphasise);
  }

  public static class AggregatedUserSearchResult {
    public final PublicUser user;
    public final List<PublicChannel> matchedChannels;
    public final List<PublicChannel> otherChannels;

    public AggregatedUserSearchResult(PublicUser user, List<PublicChannel> matchedChannels, List<PublicChannel> otherChannels) {
      this.user = user;
      this.matchedChannels = matchedChannels;
      this.otherChannels = otherChannels;
    }
  }
}
