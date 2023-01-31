package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class AddModRankRequest extends ApiRequestBase {
  public int userId;
  public @Nullable String message;

  public AddModRankRequest(int userId, @Nullable String message) {
    this.userId = userId;
    this.message = message;
  }
}
