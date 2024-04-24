package dev.rebel.chatmate.api.publicObjects.chat;

import javax.annotation.Nullable;

public class PublicMessageEmoji {
  public Integer id;
  public String name;
  public String label;
  public @Nullable PublicChatImage image;

  public String getCacheKey() {
    return String.format("emoji/%d.png", this.id);
  }
}
