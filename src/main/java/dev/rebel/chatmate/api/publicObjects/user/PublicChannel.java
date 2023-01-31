package dev.rebel.chatmate.api.publicObjects.user;

import com.google.gson.annotations.SerializedName;

public class PublicChannel {
  public Integer channelId;
  public Integer defaultUserId;
  public Platform platform;
  public String displayName;

  public enum Platform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
