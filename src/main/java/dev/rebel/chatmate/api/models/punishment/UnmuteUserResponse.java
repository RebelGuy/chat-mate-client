package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.UnmuteUserResponse.UnmuteUserResponseData;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class UnmuteUserResponse extends ApiResponseBase<UnmuteUserResponseData> {
  public static class UnmuteUserResponseData {
    public PublicUserRank removedPunishment;
  }
}
