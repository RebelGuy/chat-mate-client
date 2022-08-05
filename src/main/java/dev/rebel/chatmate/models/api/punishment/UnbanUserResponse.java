package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.UnbanUserResponse.UnbanUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicChannelPunishment;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class UnbanUserResponse extends ApiResponseBase<UnbanUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 3; }

  public static class UnbanUserResponseData {
    public @Nullable PublicUserRank updatedPunishment;
    public PublicChannelPunishment[] channelPunishments;
  }
}
