package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;

public class GetLivestreamsResponse extends ApiResponseBase<GetLivestreamsResponse.GetLivestreamsResponseData> {
  public static class GetLivestreamsResponseData {
    public PublicLivestream[] livestreams;
  }
}
