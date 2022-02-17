package dev.rebel.chatmate.models.publicObjects.chat;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

public class PublicMessageText extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String text;
  public Boolean isBold;
  public Boolean isItalics;
}
