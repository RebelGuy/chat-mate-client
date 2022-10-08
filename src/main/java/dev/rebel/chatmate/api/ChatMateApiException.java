package dev.rebel.chatmate.api;

import dev.rebel.chatmate.api.proxy.ApiResponseBase.ApiResponseError;

public class ChatMateApiException extends Exception {
  public final ApiResponseError apiResponseError;

  public ChatMateApiException(ApiResponseError apiResponseError) {
    super(String.format("Encountered ChatMate response error code %d with message: %s", apiResponseError.errorCode, apiResponseError.message));
    this.apiResponseError = apiResponseError;
  }
}
