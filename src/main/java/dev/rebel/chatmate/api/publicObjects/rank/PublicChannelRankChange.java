package dev.rebel.chatmate.api.publicObjects.rank;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class PublicChannelRankChange {
  public Integer channelId;
  public Platform platform;
  public String channelName;
  public @Nullable String error;

  public enum Platform {
    @SerializedName("youtube") YOUTUBE,
    @SerializedName("twitch") TWITCH
  }
}
