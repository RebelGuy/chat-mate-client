package dev.rebel.chatmate.api.models.streamer;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;

public class GetStreamersResponse extends ApiResponseBase<GetStreamersResponse.GetStreamersResponseData> {
  public static class GetStreamersResponseData {
    public PublicStreamerSummary[] streamers;
  }
}
