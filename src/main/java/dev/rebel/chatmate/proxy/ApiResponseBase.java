package dev.rebel.chatmate.proxy;

public abstract class ApiResponseBase {
  // the actual schema from the server
  public Number schema;

  // should return the expected schema of this response (hardcoded in Java)
  public abstract Number GetExpectedSchema();
}
