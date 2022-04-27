package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.RevokeTimeoutResponse.RevokeTimeoutResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class RevokeTimeoutResponse extends ApiResponseBase<RevokeTimeoutResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class RevokeTimeoutResponseData {
    public PublicPunishment updatedPunishment;
  }
}
