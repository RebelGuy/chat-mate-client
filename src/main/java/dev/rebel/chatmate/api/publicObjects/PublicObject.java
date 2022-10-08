package dev.rebel.chatmate.api.publicObjects;

public abstract class PublicObject {
  /* The server's schema version of the public object. */
  public Integer schema;

  /* Should return the expected schema of this object (hardcoded in Java). */
  public abstract Integer GetExpectedSchema();
}
