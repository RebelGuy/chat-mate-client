package dev.rebel.chatmate.api.proxy;

import java.util.StringJoiner;

public abstract class ApiResponseBase<Data> {
  /* The server's schema version of the response object. */
  public Integer schema;

  /* The timestamp at which the response was generated. */
  public Long timestamp;

  /* Whether the request was process correctly. */
  public Boolean success;

  public Data data;

  public ApiResponseError error;

  /* Should return the expected schema of this response (hardcoded in Java). */
  public abstract Integer GetExpectedSchema();

  /** Ensures that the base structure of the response follows the expectations. */
  public final void assertIntegrity() throws Exception {
    StringJoiner joiner = new StringJoiner(" ");
    if (this.schema == null) joiner.add("The response object's `schema` property is null.");
    if (this.timestamp == null) joiner.add("The response object's `timestamp` property is null.");
    if (this.success == null) joiner.add("The response object's `success` property is null.");
    if (this.success && this.data == null) joiner.add("The response object's `data` property is null, but `success` is true.");
    if (!this.success) {
      if (this.error == null) {
        joiner.add("The response object's `data` property is null, but `success` is true.");
      } else {
        if (this.error.errorCode == null) joiner.add("The response object's `error!.errorCode` property is null.");
        if (this.error.errorType == null) joiner.add("The response object's `error!.errorType` property is null.");
      }
    }

    if (joiner.length() > 0) {
      throw new Exception(joiner.toString());
    }
  }

  public static class ApiResponseError {
    public Integer errorCode;
    public String errorType;
    public String message;
  }
}
