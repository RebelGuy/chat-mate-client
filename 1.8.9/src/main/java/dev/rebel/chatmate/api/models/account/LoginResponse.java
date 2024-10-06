package dev.rebel.chatmate.api.models.account;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class LoginResponse extends ApiResponseBase<LoginResponse.LoginResponseData> {
  public static class LoginResponseData {
    public String loginToken;
    public @Nullable String displayName;
    public Boolean isStreamer;
  }
}
