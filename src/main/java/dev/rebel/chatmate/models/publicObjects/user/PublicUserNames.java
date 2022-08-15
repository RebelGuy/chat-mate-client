package dev.rebel.chatmate.models.publicObjects.user;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicUserNames extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public PublicUser user;
  public String[] youtubeChannelNames;
  public String[] twitchChannelNames;
}
