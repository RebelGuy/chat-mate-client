package dev.rebel.chatmate.api.models.account;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class AuthenticateResponse extends ApiResponseBase<AuthenticateResponse.AuthenticateResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class AuthenticateResponseData {
    public String username;
  }
}
