package dev.rebel.chatmate.api.publicObjects.chat;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

public class PublicChatItem {
  public Integer id;
  public Long timestamp;
  public ChatPlatform platform;
  public Boolean isCommand;
  public PublicMessagePart[] messageParts;
  public PublicUser author;

  public enum ChatPlatform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
