package dev.rebel.chatmate.api.models.rank;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class AddUserRankRequest extends ApiRequestBase {
  public AddRankName rank;
  public int userId;
  public @Nullable String message;
  public @Nullable Integer durationSeconds;

  public AddUserRankRequest(AddRankName rank, int userId, @Nullable String message, @Nullable Integer durationSeconds) {
    this.rank = rank;
    this.userId = userId;
    this.message = message;
    this.durationSeconds = durationSeconds;
  }

  public enum AddRankName {
    @SerializedName("famous") FAMOUS,
    @SerializedName("donator") DONATOR,
    @SerializedName("supporter") SUPPORTER,
    @SerializedName("member") MEMBER
  }
}
