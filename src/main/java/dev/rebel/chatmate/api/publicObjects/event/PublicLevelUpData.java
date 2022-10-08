package dev.rebel.chatmate.api.publicObjects.event;

import dev.rebel.chatmate.api.publicObjects.PublicObject;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

public class PublicLevelUpData extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public Integer oldLevel;
  public Integer newLevel;
  public PublicUser user;
}
