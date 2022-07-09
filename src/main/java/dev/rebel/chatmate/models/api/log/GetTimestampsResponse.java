package dev.rebel.chatmate.models.api.log;

import dev.rebel.chatmate.models.api.log.GetTimestampsResponse.GetTimestampsResponseData;
import dev.rebel.chatmate.models.publicObjects.log.PublicLogTimestamps;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetTimestampsResponse extends ApiResponseBase<GetTimestampsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetTimestampsResponseData {
    public PublicLogTimestamps timestamps;
  }
}
