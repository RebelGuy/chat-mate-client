package dev.rebel.chatmate.api.publicObjects.event;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicNewTwitchFollowerData extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String displayName;
}
