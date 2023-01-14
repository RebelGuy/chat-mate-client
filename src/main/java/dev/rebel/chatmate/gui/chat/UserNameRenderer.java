package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;
import dev.rebel.chatmate.api.publicObjects.user.PublicUserSearchResults;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRenderer;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class UserNameRenderer extends PaginationRenderer<PublicUserSearchResults> {
  private final MessageService messageService;

  public UserNameRenderer(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public IChatComponent renderItem(PublicUserSearchResults item, PublicUserSearchResults[] allItemsOnPage, FontEngine fontEngine, Dim chatWidth, Dim effectiveChatWidth) {
    List<Dim> allNameWidths = Collections.map(Collections.list(allItemsOnPage), x -> fontEngine.getStringWidthDim(x.matchedChannel.displayName));
    Dim width = Dim.max(effectiveChatWidth.over(2), Dim.max(allNameWidths)); // why was it done this way??

    return this.messageService.getChannelNamesMessage(chatWidth,
        item.user,
        item.matchedChannel.displayName,
        item.matchedChannel.platform == PublicChannel.Platform.Youtube ? ChatPlatform.Youtube : ChatPlatform.Twitch,
        false);
  }
}
