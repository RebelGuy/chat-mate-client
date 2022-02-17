package dev.rebel.chatmate.models.publicObjects.chat;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicMessageEmoji extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String name;
  public String label;
  public PublicChatImage image;
}
