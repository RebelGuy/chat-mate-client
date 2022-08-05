package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.UnmuteUserResponse.UnmuteUserResponseData;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class UnmuteUserResponse extends ApiResponseBase<UnmuteUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class UnmuteUserResponseData {
    public @Nullable
    PublicUserRank updatedPunishment;
  }
}
