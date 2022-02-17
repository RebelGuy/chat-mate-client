package dev.rebel.chatmate.models.publicObjects.event;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicChatMateEvent extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public ChatMateEventType type;
  public Long timestamp;
  public PublicLevelUpData data;

  public enum ChatMateEventType {
    @SerializedName("levelUp") LEVEL_UP
  }
}
