package dev.rebel.chatmate.models.publicObjects.user;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicLevelInfo extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer level;
  public Float levelProgress;
}
