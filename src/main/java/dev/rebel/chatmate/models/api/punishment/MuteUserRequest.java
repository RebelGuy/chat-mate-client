package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class MuteUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;
  public final int durationSeconds;

  public MuteUserRequest(int userId, @Nullable String message, int durationSeconds) {
    super(1);
    this.userId = userId;
    this.message = message;
    this.durationSeconds = durationSeconds;
  }
}
