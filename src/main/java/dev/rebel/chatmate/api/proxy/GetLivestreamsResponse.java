package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;

public class GetLivestreamsResponse extends ApiResponseBase<GetLivestreamsResponse.GetLivestreamsResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetLivestreamsResponseData {
    public PublicLivestream[] livestreams;
  }
}
