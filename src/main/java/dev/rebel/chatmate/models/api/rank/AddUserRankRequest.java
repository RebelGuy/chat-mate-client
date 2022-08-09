package dev.rebel.chatmate.models.api.rank;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class AddUserRankRequest extends ApiRequestBase {
  public AddRankName rank;
  public int userId;
  public @Nullable String message;
  public @Nullable Long expirationTime;

  public AddUserRankRequest(AddRankName rank, int userId, @Nullable String message, @Nullable Long expirationTime) {
    super(1);
    this.rank = rank;
    this.userId = userId;
    this.message = message;
    this.expirationTime = expirationTime;
  }

  public enum AddRankName {
    @SerializedName("famous") FAMOUS,
    @SerializedName("donator") DONATOR,
    @SerializedName("supporter") SUPPORTER,
    @SerializedName("member") MEMBER
  }
}
