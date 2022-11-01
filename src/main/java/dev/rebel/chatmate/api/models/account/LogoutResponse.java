package dev.rebel.chatmate.api.models.account;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class LogoutResponse extends ApiResponseBase<LogoutResponse.LogoutResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class LogoutResponseData {
  }
}
