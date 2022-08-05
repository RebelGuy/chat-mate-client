package dev.rebel.chatmate.models.api.experience;

import dev.rebel.chatmate.models.api.experience.ModifyExperienceResponse.ModifyExperienceResponseData;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class ModifyExperienceResponse extends ApiResponseBase<ModifyExperienceResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 3;
  }

  public static class ModifyExperienceResponseData {
    public PublicUser updatedUser;
  }
}
