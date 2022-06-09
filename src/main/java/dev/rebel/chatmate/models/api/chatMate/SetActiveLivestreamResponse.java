package dev.rebel.chatmate.models.api.chatMate;

import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamResponse.SetActiveLivestreamResponseData;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class SetActiveLivestreamResponse extends ApiResponseBase<SetActiveLivestreamResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 2; }

  public static class SetActiveLivestreamResponseData {
    public @Nullable String livestreamLink;
  }
}
