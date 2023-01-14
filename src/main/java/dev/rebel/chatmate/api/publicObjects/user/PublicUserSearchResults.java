package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicUserSearchResults extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public PublicUser user;
  public PublicChannel matchedChannel;
  public PublicChannel[] allChannels;
}
