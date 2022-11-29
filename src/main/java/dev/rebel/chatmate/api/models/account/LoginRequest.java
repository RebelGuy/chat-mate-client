package dev.rebel.chatmate.api.models.account;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

public class LoginRequest extends ApiRequestBase {
  public final String username;
  public final String password;

  public LoginRequest(String username, String password) {
    super(1);
    this.username = username;
    this.password = password;
  }
}
