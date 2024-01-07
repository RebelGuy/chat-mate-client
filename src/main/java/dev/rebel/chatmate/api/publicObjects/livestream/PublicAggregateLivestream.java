package dev.rebel.chatmate.api.publicObjects.livestream;

import javax.annotation.Nullable;

public class PublicAggregateLivestream {
  public Long startTime;
  public @Nullable Long endTime;
  public PublicLivestream[] livestreams;
}
