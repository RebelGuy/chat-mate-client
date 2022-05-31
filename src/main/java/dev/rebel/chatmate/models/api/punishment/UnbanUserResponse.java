package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.UnbanUserResponse.UnbanUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicChannelPunishment;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class UnbanUserResponse extends ApiResponseBase<UnbanUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class UnbanUserResponseData {
    public @Nullable PublicPunishment updatedPunishment;
    public PublicChannelPunishment[] channelPunishments;
  }
}
