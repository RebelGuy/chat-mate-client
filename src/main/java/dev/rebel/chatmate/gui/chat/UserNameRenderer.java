package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem.ChatPlatform;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRenderer;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.api.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.util.Collections;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class UserNameRenderer extends PaginationRenderer<PublicUserNames> {
  private final MessageService messageService;

  public UserNameRenderer(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public IChatComponent renderItem(PublicUserNames item, PublicUserNames[] allItemsOnPage, FontEngine fontEngine, Dim chatWidth, Dim effectiveChatWidth) {
    List<Dim> allNameWidths = Collections.map(Collections.list(allItemsOnPage), userNames -> fontEngine.getStringWidthDim(userNames.user.userInfo.channelName));
    Dim width = Dim.max(effectiveChatWidth.over(2), Dim.max(allNameWidths)); // why was it done this way??

    return this.messageService.getChannelNamesMessage(chatWidth,
        item.user,
        item.youtubeChannelNames.length > 0 ? item.youtubeChannelNames[0] : item.twitchChannelNames[0],
        item.youtubeChannelNames.length > 0 ? ChatPlatform.Youtube : ChatPlatform.Twitch,
        false);
  }
}
