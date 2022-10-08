package dev.rebel.chatmate.api.models.chatMate;

import dev.rebel.chatmate.api.models.chatMate.SetActiveLivestreamResponse.SetActiveLivestreamResponseData;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class SetActiveLivestreamResponse extends ApiResponseBase<SetActiveLivestreamResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class SetActiveLivestreamResponseData {
    public @Nullable String livestreamLink;
  }
}
