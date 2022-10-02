package dev.rebel.chatmate.api.models.punishment;

import dev.rebel.chatmate.api.models.punishment.TimeoutUserResponse.TimeoutUserResponseData;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class TimeoutUserResponse extends ApiResponseBase<TimeoutUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 4; }

  public static class TimeoutUserResponseData {
    public @Nullable PublicUserRank newPunishment;
    public @Nullable String newPunishmentError;
    public PublicChannelRankChange[] channelPunishments;
  }
}
