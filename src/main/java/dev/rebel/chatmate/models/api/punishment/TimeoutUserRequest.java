package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class TimeoutUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;
  public final Integer durationSeconds;

  public TimeoutUserRequest(int userId, @Nullable String message, int durationSeconds) {
    super(3);
    this.userId = userId;
    this.message = message;
    this.durationSeconds = durationSeconds;
  }
}
