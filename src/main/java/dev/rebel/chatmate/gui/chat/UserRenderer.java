package dev.rebel.chatmate.gui.chat;

import dev.rebel.chatmate.gui.chat.ChatPagination.PaginationRenderer;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.services.MessageService;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserRenderer extends PaginationRenderer<PublicUser> {
  private final MessageService messageService;

  public UserRenderer(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public IChatComponent renderItem(PublicUser item, PublicUser[] allItemsOnPage, FontRenderer fontRenderer, int chatWidth, int effectiveChatWidth) {
    List<Integer> allNameWidths = Arrays.stream(allItemsOnPage).map(user -> fontRenderer.getStringWidth(user.userInfo.channelName)).collect(Collectors.toList());
    int width = Math.max(effectiveChatWidth / 2, Collections.max(allNameWidths));

    return this.messageService.getUserMessage(item, width);
  }
}
