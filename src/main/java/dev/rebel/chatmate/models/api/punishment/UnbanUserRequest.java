package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class UnbanUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;

  public UnbanUserRequest(int userId, @Nullable String message) {
    super(1);
    this.userId = userId;
    this.message = message;
  }
}
