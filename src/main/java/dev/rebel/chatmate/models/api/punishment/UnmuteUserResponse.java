package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.UnmuteUserResponse.UnmuteUserResponseData;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class UnmuteUserResponse extends ApiResponseBase<UnmuteUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public static class UnmuteUserResponseData {
    public PublicUserRank removedPunishment;
  }
}
