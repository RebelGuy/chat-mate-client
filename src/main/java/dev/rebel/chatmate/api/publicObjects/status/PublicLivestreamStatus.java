package dev.rebel.chatmate.api.publicObjects.status;

import dev.rebel.chatmate.api.publicObjects.PublicObject;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;

import javax.annotation.Nullable;

public class PublicLivestreamStatus extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public PublicLivestream livestream;
  public @Nullable Integer youtubeLiveViewers;
  public @Nullable Integer twitchLiveViewers;
}
