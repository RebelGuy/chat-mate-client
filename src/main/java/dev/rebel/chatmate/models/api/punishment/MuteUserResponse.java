package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.MuteUserResponse.MuteUserResponseData;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class MuteUserResponse extends ApiResponseBase<MuteUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class MuteUserResponseData {
    public PublicUserRank newPunishment;
  }
}
