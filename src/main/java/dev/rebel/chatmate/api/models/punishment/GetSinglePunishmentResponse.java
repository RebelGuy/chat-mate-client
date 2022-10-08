package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.GetSinglePunishmentResponse.GetSinglePunishmentResponseData;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetSinglePunishmentResponse extends ApiResponseBase<GetSinglePunishmentResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 2;
  }

  public static class GetSinglePunishmentResponseData {
    public PublicUserRank punishment;
  }
}
