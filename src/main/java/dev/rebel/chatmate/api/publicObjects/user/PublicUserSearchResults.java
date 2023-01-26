package dev.rebel.chatmate.api.publicObjects.user;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicUserSearchResults extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public PublicUser user;
  public @Nullable PublicChannel matchedChannel;
  public PublicChannel[] allChannels;
}
