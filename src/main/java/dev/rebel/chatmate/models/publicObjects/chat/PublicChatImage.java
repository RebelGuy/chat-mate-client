package dev.rebel.chatmate.models.publicObjects.chat;

import dev.rebel.chatmate.models.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicChatImage extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String url;
  public @Nullable Integer width;
  public @Nullable Integer height;
}
