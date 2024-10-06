package dev.rebel.chatmate_1_21_1.api.publicObjects.user;

import javax.annotation.Nullable;

public class PublicUserSearchResults {
  public PublicUser user;
  public @Nullable PublicChannel matchedChannel;
  public PublicChannel[] allChannels;
}
