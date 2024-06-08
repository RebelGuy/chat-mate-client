package dev.rebel.chatmate.api.models.websocket;

import com.google.gson.annotations.SerializedName;

public enum Topic {
  @SerializedName("streamerChat") STREAMER_CHAT,
  @SerializedName("streamerEvents") STREAMER_EVENTS
}
