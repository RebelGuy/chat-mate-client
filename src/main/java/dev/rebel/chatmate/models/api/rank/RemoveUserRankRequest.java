package dev.rebel.chatmate.models.api.rank;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class RemoveUserRankRequest extends ApiRequestBase {
  public RemoveRankName rank;
  public int userId;
  public @Nullable String message;

  public RemoveUserRankRequest(RemoveRankName rank, int userId, @Nullable String message) {
    super(1);
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
