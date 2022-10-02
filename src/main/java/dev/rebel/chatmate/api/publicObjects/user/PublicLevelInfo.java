package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicLevelInfo extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer level;
  public Float levelProgress;
}
