package dev.rebel.chatmate.models.api.chatMate;

import dev.rebel.chatmate.proxy.ApiRequestBase;

import javax.annotation.Nullable;

public class SetActiveLivestreamRequest extends ApiRequestBase {
  public final @Nullable String livestream;

  public SetActiveLivestreamRequest(@Nullable String livestream) {
    super(2);
    this.livestream = livestream;
  }
}
