package dev.rebel.chatmate.api.models.experience;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class ModifyExperienceRequest extends ApiRequestBase {
  private final int userId;
  private final float deltaLevels;
  private final @Nullable String message;

  public ModifyExperienceRequest(int userId, float deltaLevels, @Nullable String message) {
    this.userId = userId;
    this.deltaLevels = deltaLevels;
    this.message = message;
  }
}
