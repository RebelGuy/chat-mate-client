package dev.rebel.chatmate.api.publicObjects.rank;

import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;

import javax.annotation.Nullable;

public class PublicChannelRankChange {
  public PublicChannel channel;
  public @Nullable String error;
}
