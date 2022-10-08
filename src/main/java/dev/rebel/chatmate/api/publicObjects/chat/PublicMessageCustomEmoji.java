package dev.rebel.chatmate.api.publicObjects.chat;

import dev.rebel.chatmate.api.publicObjects.PublicObject;
import dev.rebel.chatmate.api.publicObjects.emoji.PublicCustomEmoji;

import javax.annotation.Nullable;

public class PublicMessageCustomEmoji extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public @Nullable PublicMessageText textData;
  public @Nullable PublicMessageEmoji emojiData;
  public PublicCustomEmoji customEmoji;
}
