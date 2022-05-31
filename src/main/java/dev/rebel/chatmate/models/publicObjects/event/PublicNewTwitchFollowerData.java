package dev.rebel.chatmate.models.publicObjects.event;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicNewTwitchFollowerData extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String displayName;
}
