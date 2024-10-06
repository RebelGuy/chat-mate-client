package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.GetSinglePunishmentResponse.GetSinglePunishmentResponseData;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetSinglePunishmentResponse extends ApiResponseBase<GetSinglePunishmentResponseData> {
  public static class GetSinglePunishmentResponseData {
    public PublicUserRank punishment;
  }
}
