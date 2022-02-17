package dev.rebel.chatmate.models.api.chatMate;

import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.models.publicObjects.status.PublicApiStatus;
import dev.rebel.chatmate.models.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetStatusResponse extends ApiResponseBase<GetStatusResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetStatusResponseData {
    public PublicLivestreamStatus livestreamStatus;
    public PublicApiStatus apiStatus;
  }
}
