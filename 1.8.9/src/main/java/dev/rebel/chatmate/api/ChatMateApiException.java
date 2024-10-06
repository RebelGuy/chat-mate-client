package dev.rebel.chatmate.api;

import dev.rebel.chatmate.api.proxy.ApiResponseBase.ApiResponseError;

import javax.annotation.Nullable;

public class ChatMateApiException extends Exception {
  public final ApiResponseError apiResponseError;
  public final @Nullable String loginToken;

  public ChatMateApiException(ApiResponseError apiResponseError, @Nullable String loginToken) {
    super(String.format("Encountered ChatMate response error code %d with message: %s", apiResponseError.errorCode, apiResponseError.message));
    this.apiResponseError = apiResponseError;
    this.loginToken = loginToken;
  }
}
