package dev.rebel.chatmate.api.publicObjects.user;

import com.google.gson.annotations.SerializedName;

public class PublicChannelInfo {
  public Integer defaultUserId;
  public String externalIdOrUserName;
  public Platform platform;
  public String channelName;

  public enum Platform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
