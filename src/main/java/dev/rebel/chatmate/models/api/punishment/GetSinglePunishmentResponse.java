package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.GetSinglePunishmentResponse.GetSinglePunishmentResponseData;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetSinglePunishmentResponse extends ApiResponseBase<GetSinglePunishmentResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 2;
  }

  public static class GetSinglePunishmentResponseData {
    public PublicUserRank punishment;
  }
}
