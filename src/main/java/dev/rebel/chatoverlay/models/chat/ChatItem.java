package dev.rebel.chatoverlay.models.chat;

import javax.annotation.Nullable;
import java.util.List;

public class ChatItem {
  public Long internalId;
  public String id;

  // unix timestamp (in milliseconds)
  public Long timestamp;
  public Author author;
  public List<PartialChatMessage> messageParts;

  // the message conversion to pure text
  public String renderedText;
}
