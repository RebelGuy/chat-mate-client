package dev.rebel.chatmate.api.proxy;

public abstract class ApiRequestBase {
  /* The schema version of the request object. */
  public final Integer schema;

  public ApiRequestBase(int schema) {
    this.schema = schema;
  }
}
