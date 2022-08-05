package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class UnmuteUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;

  public UnmuteUserRequest(int userId, @Nullable String message) {
    super(2);
    this.userId = userId;
    this.message = message;
  }
}
