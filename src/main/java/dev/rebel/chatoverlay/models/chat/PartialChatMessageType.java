package dev.rebel.chatoverlay.models.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PartialChatMessageType {
  @JsonProperty("text") text,
  @JsonProperty("emoji") emoji
}
