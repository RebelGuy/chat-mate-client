package dev.rebel.chatmate.models.configMigrations;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV0;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV1;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.Version;
import dev.rebel.chatmate.models.VersionedData;
import dev.rebel.chatmate.services.util.Collections;

import java.util.List;

/** Represents a model of version `V` that can be migrated to version `V+1`. */
public abstract class Migration<CurrentModel extends Version, NextModel extends Version> {
  private final static Class<Migration<?, ?>>[] migrations = new Class[] { v0v1.class };
  private final static Class<Version>[] versions = new Class[] { SerialisedConfigV0.class, SerialisedConfigV1.class };

  protected final CurrentModel data;

  public Migration(CurrentModel currentModel) {
    this.data = currentModel;
  }

  abstract NextModel up();

  public static <TargetVersion extends Version> TargetVersion parseAndMigrate(VersionedData data) {
    Version parsed = data.parseData(versions[data.schema]);
    return (TargetVersion)migrate(parsed);
  }

  private static Version migrate(Version version) {
    while (version.getVersion() < migrations.length) {
      int currentSchema = version.getVersion();
      try {
        version = migrations[currentSchema].getConstructor(versions[currentSchema]).newInstance(version).up();
      } catch (Exception ignored) {
        throw new RuntimeException(String.format("Unable to migrate to version %d because the constructor of the migration is unexpected.", currentSchema));
      }
    }

    return version;
  }
}
