package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatItem;

public class NewChatEventData {
  public final PublicChatItem[] chatItems;

  public NewChatEventData(PublicChatItem[] chatItems) {
    this.chatItems = chatItems;
  }
}
