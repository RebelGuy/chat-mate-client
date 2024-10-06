package dev.rebel.chatmate.api.models.chatMate;

import dev.rebel.chatmate.api.models.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.api.publicObjects.status.PublicApiStatus;
import dev.rebel.chatmate.api.publicObjects.status.PublicLivestreamStatus;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetStatusResponse extends ApiResponseBase<GetStatusResponseData> {
  public static class GetStatusResponseData {
    public PublicLivestreamStatus livestreamStatus;
    public PublicApiStatus youtubeApiStatus;
    public PublicApiStatus twitchApiStatus;
  }
}
