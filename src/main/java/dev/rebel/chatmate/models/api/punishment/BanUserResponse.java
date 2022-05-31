package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.BanUserResponse.BanUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicChannelPunishment;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class BanUserResponse extends ApiResponseBase<BanUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class BanUserResponseData {
    public PublicPunishment newPunishment;
    public PublicChannelPunishment[] channelPunishments;
  }
}
