package dev.rebel.chatmate.models.publicObjects.status;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;
import dev.rebel.chatmate.models.publicObjects.livestream.PublicLivestream;

import javax.annotation.Nullable;

public class PublicLivestreamStatus extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public PublicLivestream livestream;
  public @Nullable Integer youtubeLiveViewers;
  public @Nullable Integer twitchLiveViewers;
}
