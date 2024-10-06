package dev.rebel.chatmate.api.publicObjects.streamer;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;

import javax.annotation.Nullable;

public class PublicStreamerSummary {
  public String username;
  public @Nullable String displayName;
  public @Nullable PublicLivestream currentYoutubeLivestream;
  public @Nullable PublicLivestream currentTwitchLivestream;
  public @Nullable PublicChannel youtubeChannel;
  public @Nullable PublicChannel twitchChannel;

  public boolean isYoutubeLive() {
    return this.currentYoutubeLivestream != null && this.currentYoutubeLivestream.status == PublicLivestream.LivestreamStatus.Live;
  }

  public boolean isTwitchLive() {
    return this.currentTwitchLivestream != null && this.currentTwitchLivestream.status == PublicLivestream.LivestreamStatus.Live;
  }
}
