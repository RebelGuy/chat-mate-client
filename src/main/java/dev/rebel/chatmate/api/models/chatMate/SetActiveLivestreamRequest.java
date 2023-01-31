package dev.rebel.chatmate.api.models.chatMate;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class SetActiveLivestreamRequest extends ApiRequestBase {
  public final @Nullable String livestream;

  public SetActiveLivestreamRequest(@Nullable String livestream) {
    this.livestream = livestream;
  }
}
