package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.UnmuteUserResponse.UnmuteUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class UnmuteUserResponse extends ApiResponseBase<UnmuteUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class UnmuteUserResponseData {
    public PublicPunishment updatedPunishment;
  }
}
