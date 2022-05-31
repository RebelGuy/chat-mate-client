package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.MuteUserResponse.MuteUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class MuteUserResponse extends ApiResponseBase<MuteUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class MuteUserResponseData {
    public PublicPunishment newPunishment;
  }
}
