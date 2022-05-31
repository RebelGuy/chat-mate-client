package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRenderer;
import dev.rebel.chatmate.models.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.services.MessageService;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class UserNameRenderer extends PaginationRenderer<PublicUserNames> {
  private final MessageService messageService;

  public UserNameRenderer(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public IChatComponent renderItem(PublicUserNames item, PublicUserNames[] allItemsOnPage, FontRenderer fontRenderer, int chatWidth, int effectiveChatWidth) {
    List<Integer> allNameWidths = Collections.map(Collections.list(allItemsOnPage), userNames -> fontRenderer.getStringWidth(userNames.user.userInfo.channelName));
    int width = Math.max(effectiveChatWidth / 2, Collections.max(allNameWidths));

    return this.messageService.getChannelNamesMessage(item, width);
  }
}
