package dev.rebel.chatmate.models.publicObjects.log;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicLogTimestamps extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Long[] warnings;
  public Long[] errors;
}
