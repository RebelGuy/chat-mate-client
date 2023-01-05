package dev.rebel.chatmate.api.publicObjects.user;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicChannelInfo extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String externalIdOrUserName;
  public Platform platform;
  public String channelName;

  public enum Platform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
