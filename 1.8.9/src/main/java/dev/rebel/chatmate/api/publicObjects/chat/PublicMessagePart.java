package dev.rebel.chatmate.api.publicObjects.chat;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class PublicMessagePart {
  public MessagePartType type;
  public @Nullable PublicMessageText textData;
  public @Nullable PublicMessageEmoji emojiData;
  public @Nullable PublicMessageCustomEmoji customEmojiData;
  public @Nullable PublicMessageCheer cheerData;

  public enum MessagePartType {
    @SerializedName("text") text,
    @SerializedName("emoji") emoji,
    @SerializedName("customEmoji") customEmoji,
    @SerializedName("cheer") cheer
  }
}
