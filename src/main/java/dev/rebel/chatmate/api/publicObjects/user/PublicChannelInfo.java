package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicChannelInfo extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String channelName;
}
