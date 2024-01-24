package dev.rebel.chatmate.api.models.streamer;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class GetPrimaryChannelsResponse extends ApiResponseBase<GetPrimaryChannelsResponse.GetPrimaryChannelsResponseData> {
  public static class GetPrimaryChannelsResponseData {
    public @Nullable Integer youtubeChannelId;
    public @Nullable Integer twitchChannelId;
    public @Nullable String youtubeChannelName;
    public @Nullable String twitchChannelName;

    public boolean hasPrimaryYoutubeChannel() {
      return this.youtubeChannelId != null;
    }

    public boolean hasPrimaryTwitchChannel() {
      return this.twitchChannelId != null;
    }
  }
}
