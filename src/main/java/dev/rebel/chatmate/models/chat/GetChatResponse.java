package dev.rebel.chatmate.models.chat;

import dev.rebel.chatmate.models.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetChatResponse extends ApiResponseBase<GetChatResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 5;
  }

  public static class GetChatResponseData {
    public Long reusableTimestamp;
    public ChatItem[] chat;
  }

  public static class ChatItem {
    public Long internalId;
    public String id;

    // unix timestamp (in milliseconds)
    public Long timestamp;
    public Author author;
    public PartialChatMessage[] messageParts;
  }

  public static class Author {
    public Long internalId;
    public String name;
    public String channelId;
    public String image;
    public Boolean isOwner;
    public Boolean isModerator;
    public Boolean isVerified;
    public Long lastUpdate;
    public Integer level;
    public Double levelProgress;
  }

  public static class PartialChatMessage {
    public PartialChatMessageType type;

    // for text type
    public String text;
    public Boolean isBold;
    public Boolean isItalics;

    // for emoji type
    public String emojiId;
    // the hover-over name, e.g. :slightly_smiling:
    public String name;
    // short emoji label (e.g. shortcut text/search term)
    public String label;
    public ChatImage image;
  }

  public static class ChatImage {
    public String url;
    public Integer width;
    public Integer height;
  }
}
