package dev.rebel.chatmate.api.publicObjects.chat;

import dev.rebel.chatmate.api.publicObjects.emoji.PublicCustomEmoji;

import javax.annotation.Nullable;

public class PublicMessageCustomEmoji {
  public @Nullable PublicMessageText textData;
  public @Nullable PublicMessageEmoji emojiData;
  public PublicCustomEmoji customEmoji;
}
