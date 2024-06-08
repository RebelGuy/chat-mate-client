package dev.rebel.chatmate.config;

import dev.rebel.chatmate.config.serialised.SerialisedConfigVersions.Version;

import static dev.rebel.chatmate.util.JsonHelpers.parseSerialisedObject;

public class VersionedData {
  public final int schema;
  public final Object data;

  public VersionedData(int schema, Object data) {
    this.schema = schema;
    this.data = data;
  }

  public <Serialised extends Version> Serialised parseData(Class<Serialised> clazz) {
    return parseSerialisedObject(this.data, clazz);
  }
}
