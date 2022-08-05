package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.BanUserResponse.BanUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicChannelPunishment;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class BanUserResponse extends ApiResponseBase<BanUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public static class BanUserResponseData {
    public PublicUserRank newPunishment;
    public PublicChannelPunishment[] channelPunishments;
  }
}
