package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.event.PublicChatMessageDeletedData;

import java.util.Date;

public class ChatMessageDeletedEventData {
  public final Date date;
  public final PublicChatMessageDeletedData chatMessageDeletedData;

  public ChatMessageDeletedEventData(Date date, PublicChatMessageDeletedData chatMessageDeletedData) {
    this.date = date;
    this.chatMessageDeletedData = chatMessageDeletedData;
  }
}
