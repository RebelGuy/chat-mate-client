package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.RevokeTimeoutResponse.RevokeTimeoutResponseData;
import dev.rebel.chatmate.models.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class RevokeTimeoutResponse extends ApiResponseBase<RevokeTimeoutResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 4; }

  public static class RevokeTimeoutResponseData {
    public @Nullable PublicUserRank removedPunishment;
    public @Nullable String removedPunishmentError;
    public PublicChannelRankChange[] channelPunishments;
  }
}
