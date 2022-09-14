package dev.rebel.chatmate.models.publicObjects.livestream;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicLivestream extends PublicObject {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public Integer id;
  public String livestreamLink;
  public LivestreamStatus status;
  public @Nullable Long startTime;
  public @Nullable Long endTime;

  public enum LivestreamStatus {
    @SerializedName("not_started") NotStarted,
    @SerializedName("live") Live,
    @SerializedName("finished") Finished,
  }
}
