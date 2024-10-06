package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class MuteUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;
  public final int durationSeconds;

  public MuteUserRequest(int userId, @Nullable String message, int durationSeconds) {
    this.userId = userId;
    this.message = message;
    this.durationSeconds = durationSeconds;
  }
}
