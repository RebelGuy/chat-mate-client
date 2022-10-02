package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.MuteUserResponse.MuteUserResponseData;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class MuteUserResponse extends ApiResponseBase<MuteUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class MuteUserResponseData {
    public PublicUserRank newPunishment;
  }
}
