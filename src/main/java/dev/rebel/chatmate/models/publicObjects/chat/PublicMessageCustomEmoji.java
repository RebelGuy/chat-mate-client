package dev.rebel.chatmate.models.publicObjects.chat;

import dev.rebel.chatmate.models.publicObjects.PublicObject;
import dev.rebel.chatmate.models.publicObjects.emoji.PublicCustomEmoji;

public class PublicMessageCustomEmoji extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public PublicMessageText textData;
  public PublicCustomEmoji customEmoji;
}
