package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.BanUserResponse.BanUserResponseData;
import dev.rebel.chatmate.models.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class BanUserResponse extends ApiResponseBase<BanUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 4; }

  public static class BanUserResponseData {
    public @Nullable PublicUserRank newPunishment;
    public @Nullable String newPunishmentError;
    public PublicChannelRankChange[] channelPunishments;
  }
}
