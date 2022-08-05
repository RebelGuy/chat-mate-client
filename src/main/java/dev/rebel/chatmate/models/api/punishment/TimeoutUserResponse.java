package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.TimeoutUserResponse.TimeoutUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicChannelPunishment;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class TimeoutUserResponse extends ApiResponseBase<TimeoutUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public static class TimeoutUserResponseData {
    public PublicUserRank newPunishment;
    public PublicChannelPunishment[] channelPunishments;
  }
}
