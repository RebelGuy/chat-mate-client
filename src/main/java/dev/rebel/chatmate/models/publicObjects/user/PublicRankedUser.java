package dev.rebel.chatmate.models.publicObjects.user;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicRankedUser extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public Integer rank;
  public PublicUser user;
}
