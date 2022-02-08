package dev.rebel.chatmate.models;

public class VersionedData {
  public final int schema;
  public final Object data;

  public VersionedData(int schema, Object data) {
    this.schema = schema;
    this.data = data;
  }
}
