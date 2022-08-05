package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.RevokeTimeoutResponse.RevokeTimeoutResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicChannelPunishment;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class RevokeTimeoutResponse extends ApiResponseBase<RevokeTimeoutResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public static class RevokeTimeoutResponseData {
    public @Nullable PublicUserRank updatedPunishment;
    public PublicChannelPunishment[] channelPunishments;
  }
}
