package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.BanUserResponse.BanUserResponseData;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class BanUserResponse extends ApiResponseBase<BanUserResponseData> {
  public static class BanUserResponseData {
    public @Nullable PublicUserRank newPunishment;
    public @Nullable String newPunishmentError;
    public PublicChannelRankChange[] channelPunishments;
  }
}
