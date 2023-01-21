package dev.rebel.chatmate.api.publicObjects.user;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicChannel extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer channelId;
  public Integer defaultUserId;
  public Platform platform;
  public String displayName;

  public enum Platform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
