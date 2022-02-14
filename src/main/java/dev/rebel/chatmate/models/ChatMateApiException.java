package dev.rebel.chatmate.models;

import dev.rebel.chatmate.proxy.ApiResponseBase.ApiResponseError;

public class ChatMateApiException extends Exception {
  public final ApiResponseError apiResponseError;

  public ChatMateApiException(ApiResponseError apiResponseError) {
    super(String.format("Encountered API response error code %d with message: %s", apiResponseError.errorCode, apiResponseError.message));
    this.apiResponseError = apiResponseError;
  }
}
