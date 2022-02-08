package dev.rebel.chatmate.models.configMigrations;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.Version;
import dev.rebel.chatmate.models.VersionedData;

/** Represents a model of version `V` that can be migrated to version `V+1`. */
public abstract class Migration<CurrentModel extends Version, NextModel extends Version> {
  protected final CurrentModel data;

  public Migration(Class<CurrentModel> clazz, Object rawData) {
    if (rawData instanceof LinkedTreeMap) {
      this.data = new Gson().fromJson(new Gson().toJson(rawData), clazz);
    } else if (clazz.isInstance(rawData)) {
      this.data = (CurrentModel)rawData;
    } else {
      throw new RuntimeException("Cannot initialise migration for model " + clazz.getSimpleName());
    }
  }

  abstract NextModel up();
}
