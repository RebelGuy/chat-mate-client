package dev.rebel.chatmate.api.models.log;

import dev.rebel.chatmate.api.models.log.GetTimestampsResponse.GetTimestampsResponseData;
import dev.rebel.chatmate.api.publicObjects.log.PublicLogTimestamps;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetTimestampsResponse extends ApiResponseBase<GetTimestampsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetTimestampsResponseData {
    public PublicLogTimestamps timestamps;
  }
}