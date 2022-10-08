package dev.rebel.chatmate.api.publicObjects.chat;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicMessageCheer extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String name;
  public Integer amount;
  public String imageUrl;
  public String colour;
}
