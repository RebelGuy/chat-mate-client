package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class RevokeTimeoutRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;

  public RevokeTimeoutRequest(int userId, @Nullable String message) {
    this.userId = userId;
    this.message = message;
  }
}
