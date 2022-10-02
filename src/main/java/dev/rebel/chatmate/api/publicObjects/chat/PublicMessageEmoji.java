package dev.rebel.chatmate.api.publicObjects.chat;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicMessageEmoji extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String name;
  public String label;
  public PublicChatImage image;
}
