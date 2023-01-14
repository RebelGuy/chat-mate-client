package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicRegisteredUser extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public String displayName;
}
