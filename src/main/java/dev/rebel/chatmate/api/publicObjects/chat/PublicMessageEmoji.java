package dev.rebel.chatmate.api.publicObjects.chat;

public class PublicMessageEmoji {
  public Integer id;
  public String name;
  public String label;
  public PublicChatImage image;

  public String getCacheKey() {
    return String.format("emoji/%d.png", this.id);
  }
}
