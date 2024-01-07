package dev.rebel.chatmate.api.publicObjects.status;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;

import javax.annotation.Nullable;

public class PublicLivestreamStatus {
  public @Nullable PublicLivestream youtubeLivestream;
  public @Nullable Integer youtubeLiveViewers;
  public @Nullable PublicLivestream twitchLivestream;
  public @Nullable Integer twitchLiveViewers;

  public boolean isYoutubeLive() {
    return this.youtubeLivestream != null && this.youtubeLivestream.status == PublicLivestream.LivestreamStatus.Live;
  }

  public @Nullable Long getYoutubeStartTime() {
    return this.youtubeLivestream == null ? null : this.youtubeLivestream.startTime;
  }

  public boolean isTwitchLive() {
    return this.twitchLivestream != null && this.twitchLivestream.status == PublicLivestream.LivestreamStatus.Live;
  }

  public @Nullable Long getTwitchStartTime() {
    return this.twitchLivestream == null ? null : this.twitchLivestream.startTime;
  }
}
