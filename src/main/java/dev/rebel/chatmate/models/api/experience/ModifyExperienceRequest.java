package dev.rebel.chatmate.models.api.experience;

import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class ModifyExperienceRequest extends ApiRequestBase {
  private final int userId;
  private final float deltaLevels;
  private final @Nullable String message;

  public ModifyExperienceRequest(int userId, float deltaLevels, @Nullable String message) {
    super(3);
    this.userId = userId;
    this.deltaLevels = deltaLevels;
    this.message = message;
  }
}
