package dev.rebel.chatmate.api.publicObjects.status;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;

import javax.annotation.Nullable;

public class PublicLivestreamStatus {
  public PublicLivestream livestream;
  public @Nullable Integer youtubeLiveViewers;
  public @Nullable Integer twitchLiveViewers;
}
