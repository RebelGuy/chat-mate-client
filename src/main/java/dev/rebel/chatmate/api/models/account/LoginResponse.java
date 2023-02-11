package dev.rebel.chatmate.api.models.account;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class LoginResponse extends ApiResponseBase<LoginResponse.LoginResponseData> {
  public static class LoginResponseData {
    public String loginToken;
  }
}
