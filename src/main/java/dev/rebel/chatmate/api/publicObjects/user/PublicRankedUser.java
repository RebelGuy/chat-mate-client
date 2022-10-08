package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicRankedUser extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public Integer rank;
  public PublicUser user;
}
