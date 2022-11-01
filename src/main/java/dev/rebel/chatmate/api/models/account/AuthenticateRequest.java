package dev.rebel.chatmate.api.models.account;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

public class AuthenticateRequest extends ApiRequestBase {
  public final String loginToken;

  public AuthenticateRequest(String loginToken) {
    super(1);
    this.loginToken = loginToken;
  }
}
