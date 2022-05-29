package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.RevokeTimeoutResponse.RevokeTimeoutResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicChannelPunishment;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class RevokeTimeoutResponse extends ApiResponseBase<RevokeTimeoutResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class RevokeTimeoutResponseData {
    public @Nullable PublicPunishment updatedPunishment;
    public PublicChannelPunishment[] channelPunishments;
  }
}
