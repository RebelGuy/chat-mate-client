package dev.rebel.chatmate.models.publicObjects.chat;

import dev.rebel.chatmate.models.publicObjects.PublicObject;
import dev.rebel.chatmate.models.publicObjects.emoji.PublicCustomEmoji;

import javax.annotation.Nullable;

public class PublicMessageCustomEmoji extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public @Nullable PublicMessageText textData;
  public @Nullable PublicMessageEmoji emojiData;
  public PublicCustomEmoji customEmoji;
}
