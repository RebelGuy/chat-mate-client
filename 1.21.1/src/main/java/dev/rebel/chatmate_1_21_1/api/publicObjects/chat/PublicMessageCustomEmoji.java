package dev.rebel.chatmate_1_21_1.api.publicObjects.chat;

import dev.rebel.chatmate_1_21_1.api.publicObjects.emoji.PublicCustomEmoji;

import javax.annotation.Nullable;

public class PublicMessageCustomEmoji {
  public @Nullable PublicMessageText textData;
  public @Nullable PublicMessageEmoji emojiData;
  public PublicCustomEmoji customEmoji;
}
