package dev.rebel.chatmate.models.api.punishment;

import dev.rebel.chatmate.models.api.punishment.GetPunishmentsResponse.GetPunishmentsResponseData;
import dev.rebel.chatmate.models.publicObjects.punishment.PublicPunishment;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetPunishmentsResponse extends ApiResponseBase<GetPunishmentsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetPunishmentsResponseData {
    public PublicPunishment[] punishments;
  }
}
