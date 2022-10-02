package dev.rebel.chatmate.api.publicObjects.chat;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

public class PublicMessageText extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String text;
  public Boolean isBold;
  public Boolean isItalics;
}
