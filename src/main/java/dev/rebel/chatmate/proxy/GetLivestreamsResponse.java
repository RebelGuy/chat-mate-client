package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.publicObjects.livestream.PublicLivestream;

public class GetLivestreamsResponse extends ApiResponseBase<GetLivestreamsResponse.GetLivestreamsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetLivestreamsResponseData {
    public PublicLivestream[] livestreams;
  }
}
