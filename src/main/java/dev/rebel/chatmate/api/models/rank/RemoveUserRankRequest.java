package dev.rebel.chatmate.api.models.rank;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class RemoveUserRankRequest extends ApiRequestBase {
  public RemoveRankName rank;
  public int userId;
  public @Nullable String message;

  public RemoveUserRankRequest(RemoveRankName rank, int userId, @Nullable String message) {
    this.rank = rank;
    this.userId = userId;
    this.message = message;
  }

  public enum RemoveRankName {
    @SerializedName("famous") FAMOUS,
    @SerializedName("donator") DONATOR,
    @SerializedName("supporter") SUPPORTER,
    @SerializedName("member") MEMBER
  }
}
