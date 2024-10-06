package dev.rebel.chatmate.api.publicObjects.chat;

import com.google.gson.annotations.SerializedName;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

import javax.annotation.Nullable;

public class PublicChatItem {
  public Integer id;
  public Long timestamp;
  public ChatPlatform platform;
  public @Nullable Integer commandId;
  public PublicMessagePart[] messageParts;
  public PublicUser author;

  public enum ChatPlatform {
    @SerializedName("youtube") Youtube,
    @SerializedName("twitch") Twitch
  }
}
