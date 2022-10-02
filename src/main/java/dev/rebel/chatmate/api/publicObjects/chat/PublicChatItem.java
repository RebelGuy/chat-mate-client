package dev.rebel.chatmate.api.publicObjects.chat;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.publicObjects.PublicObject;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

public class PublicChatItem extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 4; }

  public Integer id;
  public Long timestamp;
  public ChatPlatform platform;
  public PublicMessagePart[] messageParts;
  public PublicUser author;

  public enum ChatPlatform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
