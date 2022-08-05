package dev.rebel.chatmate.models.publicObjects.user;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicUser extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public Integer id;
  public PublicChannelInfo userInfo;
  public PublicLevelInfo levelInfo;
  public PublicPunishment[] activePunishments;
}
