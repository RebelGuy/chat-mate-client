package dev.rebel.chatmate.api;

import javax.annotation.Nullable;

public class HttpException extends Exception {
  /** The original (possibly internal) error that caused this exception to be thrown. */
  public final String errorMessage;
  public final @Nullable Integer statusCode;
  public final @Nullable String responseBody;

  public HttpException(String errorMessage, @Nullable Integer statusCode, @Nullable String responseBody) {
    super(String.format("Encountered HTTP response error code %d", statusCode == null ? -1 : statusCode));
    this.errorMessage = errorMessage;
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }
}
