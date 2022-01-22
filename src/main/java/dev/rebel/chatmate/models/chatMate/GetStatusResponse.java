package dev.rebel.chatmate.models.chatMate;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class GetStatusResponse extends ApiResponseBase {
  public Long timestamp;
  public LivestreamStatus livestreamStatus;
  public ApiStatus apiStatus;

  @Override
  public Number GetExpectedSchema() {
    return 1;
  }

  public static class LivestreamStatus {
    public String livestreamLink;
    public Status status;
    public @Nullable Long startTime;
    public @Nullable Long endTime;
    public @Nullable Integer liveViewers;

    public enum Status {
      @SerializedName("not_started") NotStarted,
      @SerializedName("live") Live,
      @SerializedName("finished") Finished,
    }
  }

  public static class ApiStatus {
    public @Nullable Status status;
    public @Nullable Long lastOk;
    public @Nullable Long avgRoundtrip;

    public enum Status {
      @SerializedName("ok") OK,
      @SerializedName("error") Error
    }
  }
}
