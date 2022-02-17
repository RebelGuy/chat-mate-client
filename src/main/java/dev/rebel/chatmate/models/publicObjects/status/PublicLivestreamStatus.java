package dev.rebel.chatmate.models.publicObjects.status;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicLivestreamStatus extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String livestreamLink;
  public LivestreamStatus status;
  public @Nullable Long startTime;
  public @Nullable Long endTime;
  public @Nullable Integer liveViewers;

  public enum LivestreamStatus {
    @SerializedName("not_started") NotStarted,
    @SerializedName("live") Live,
    @SerializedName("finished") Finished,
  }
}
