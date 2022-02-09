package dev.rebel.chatmate.models;

import com.google.gson.Gson;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.Version;

public class VersionedData {
  public final int schema;
  public final Object data;

  public VersionedData(int schema, Object data) {
    this.schema = schema;
    this.data = data;
  }

  public <Serialised extends Version> Serialised parseData(Class<Serialised> clazz) {
    return new Gson().fromJson(new Gson().toJson(this.data), clazz);
  }
}
