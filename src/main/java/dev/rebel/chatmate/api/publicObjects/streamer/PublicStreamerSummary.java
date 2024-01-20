package dev.rebel.chatmate.api.publicObjects.streamer;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;

import javax.annotation.Nullable;

public class PublicStreamerSummary {
  public String username;
  public @Nullable PublicLivestream currentYoutubeLivestream;
  public @Nullable PublicLivestream currentTwitchLivestream;
  public @Nullable PublicChannel youtubeChannel;
  public @Nullable PublicChannel twitchChannel;
}
