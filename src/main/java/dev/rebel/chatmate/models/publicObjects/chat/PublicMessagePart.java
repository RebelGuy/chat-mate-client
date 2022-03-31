package dev.rebel.chatmate.models.publicObjects.chat;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicMessagePart extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 3; }

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
