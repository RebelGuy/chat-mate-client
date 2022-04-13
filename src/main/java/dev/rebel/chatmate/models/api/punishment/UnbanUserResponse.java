package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.UnbanUserResponse.UnbanUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class UnbanUserResponse extends ApiResponseBase<UnbanUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class UnbanUserResponseData {
    public PublicPunishment updatedPunishment;
  }
}
