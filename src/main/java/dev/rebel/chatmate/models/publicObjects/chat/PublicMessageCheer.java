package dev.rebel.chatmate.models.publicObjects.chat;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicMessageCheer extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String name;
  public Integer amount;
  public String imageUrl;
  public String colour;
}
