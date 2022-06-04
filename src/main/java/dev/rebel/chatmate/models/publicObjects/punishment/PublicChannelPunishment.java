package dev.rebel.chatmate.models.publicObjects.punishment;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicChannelPunishment extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer channelId;
  public Platform platform;
  public String channelName;
  public @Nullable String error;

  public enum Platform {
    @SerializedName("youtube") YOUTUBE,
    @SerializedName("twitch") TWITCH
  }
}