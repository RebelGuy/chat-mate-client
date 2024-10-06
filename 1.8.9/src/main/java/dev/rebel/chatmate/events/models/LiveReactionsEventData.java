package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.chat.PublicChatImage;

public class LiveReactionsEventData {
  public int emojiId;
  public PublicChatImage emojiImage;
  public int reactionCount;

  public LiveReactionsEventData(int emojiId, PublicChatImage emojiImage, int reactionCount) {
    this.emojiId = emojiId;
    this.emojiImage = emojiImage;
    this.reactionCount = reactionCount;
  }

  public String getCacheKey() {
    return String.format("emoji/%d.png", this.emojiId);
  }
}
