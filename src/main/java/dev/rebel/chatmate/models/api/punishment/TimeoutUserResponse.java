package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.TimeoutUserResponse.TimeoutUserResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class TimeoutUserResponse extends ApiResponseBase<TimeoutUserResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class TimeoutUserResponseData {
    public PublicPunishment newPunishment;
  }
}
