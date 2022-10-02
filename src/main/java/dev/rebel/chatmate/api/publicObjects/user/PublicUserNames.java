package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicUserNames extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public PublicUser user;
  public String[] youtubeChannelNames;
  public String[] twitchChannelNames;
}
