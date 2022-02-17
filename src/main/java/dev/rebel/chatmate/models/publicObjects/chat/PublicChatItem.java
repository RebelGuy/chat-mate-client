package dev.rebel.chatmate.models.publicObjects.chat;

import dev.rebel.chatmate.models.publicObjects.PublicObject;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;

public class PublicChatItem extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public Integer id;
  public Long timestamp;
  public PublicMessagePart[] messageParts;
  public PublicUser author;
}
