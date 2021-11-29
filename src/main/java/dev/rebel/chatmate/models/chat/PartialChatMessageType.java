package dev.rebel.chatmate.models.chat;

import com.google.gson.annotations.SerializedName;

public enum PartialChatMessageType {
  @SerializedName("text") text,
  @SerializedName("emoji") emoji
}
