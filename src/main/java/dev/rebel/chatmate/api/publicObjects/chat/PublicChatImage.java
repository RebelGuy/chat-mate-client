package dev.rebel.chatmate.api.publicObjects.chat;

import dev.rebel.chatmate.api.publicObjects.PublicObject;

import javax.annotation.Nullable;

public class PublicChatImage extends PublicObject {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public String url;
  public @Nullable Integer width;
  public @Nullable Integer height;
}
