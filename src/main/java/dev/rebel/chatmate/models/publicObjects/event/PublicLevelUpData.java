package dev.rebel.chatmate.models.publicObjects.event;

import dev.rebel.chatmate.models.publicObjects.PublicObject;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;

public class PublicLevelUpData extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer oldLevel;
  public Integer newLevel;
  public PublicUser user;
}
