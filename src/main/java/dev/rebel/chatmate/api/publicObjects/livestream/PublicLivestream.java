package dev.rebel.chatmate.api.publicObjects.livestream;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class PublicLivestream {
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
