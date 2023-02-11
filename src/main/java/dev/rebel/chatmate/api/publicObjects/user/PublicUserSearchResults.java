package dev.rebel.chatmate.api.publicObjects.user;

import javax.annotation.Nullable;

public class PublicUserSearchResults {
  public PublicUser user;
  public @Nullable PublicChannel matchedChannel;
  public PublicChannel[] allChannels;
}
