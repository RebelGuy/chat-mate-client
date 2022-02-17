package dev.rebel.chatmate.models.publicObjects.chat;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicMessagePart extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public MessagePartType type;
  public @Nullable PublicMessageText textData;
  public @Nullable PublicMessageEmoji emojiData;

  public enum MessagePartType {
    @SerializedName("text") text,
    @SerializedName("emoji") emoji
  }
}
