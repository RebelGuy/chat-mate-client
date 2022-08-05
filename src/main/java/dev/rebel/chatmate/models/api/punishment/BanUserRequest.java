package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class BanUserRequest extends ApiRequestBase {
  public final int userId;
  public final @Nullable String message;

  public BanUserRequest(int userId, @Nullable String message) {
    super(3);
    this.userId = userId;
    this.message = message;
  }
}
