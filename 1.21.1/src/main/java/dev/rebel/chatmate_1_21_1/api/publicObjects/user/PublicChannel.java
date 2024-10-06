package dev.rebel.chatmate_1_21_1.api.publicObjects.user;

import com.google.gson.annotations.SerializedName;

public class PublicChannel {
  public Integer channelId;
  public Integer defaultUserId;
  public String externalIdOrUserName;
  public Platform platform;
  public String displayName;
  public String channelUrl;

  public enum Platform {
    @SerializedName("youtube") YOUTUBE,
    @SerializedName("twitch") TWITCH
  }
}
