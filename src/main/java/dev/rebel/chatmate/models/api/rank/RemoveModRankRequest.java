package dev.rebel.chatmate.models.api.rank;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class RemoveModRankRequest extends ApiRequestBase {
  public int userId;
  public @Nullable String message;

  public RemoveModRankRequest(int userId, @Nullable String message) {
    super(1);
    this.userId = userId;
    this.message = message;
  }
}
