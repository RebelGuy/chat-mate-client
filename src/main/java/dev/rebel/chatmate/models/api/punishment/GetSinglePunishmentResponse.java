package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.GetSinglePunishmentResponse.GetSinglePunishmentResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetSinglePunishmentResponse extends ApiResponseBase<GetSinglePunishmentResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetSinglePunishmentResponseData {
    public PublicPunishment punishment;
  }
}
