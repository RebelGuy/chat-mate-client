package dev.rebel.chatmate.models.configMigrations;

import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.*;
import dev.rebel.chatmate.models.VersionedData;

import java.lang.reflect.Method;

/** Represents a model of version `V` that can be migrated to version `V+1`. */
public abstract class Migration<FromModel extends Version, ToModel extends Version> {
  private final static Class<Migration<?, ?>>[] migrations = new Class[] {
      v0v1.class,
      v1v2.class,
      v2v3.class,
      v3v4.class
  };
  private final static Class<Version>[] versions = new Class[] {
      SerialisedConfigV0.class,
      SerialisedConfigV1.class,
      SerialisedConfigV2.class,
      SerialisedConfigV3.class,
      SerialisedConfigV4.class
  };

  abstract ToModel up(FromModel data);

  abstract FromModel down(ToModel data);

  public static <TargetVersion extends Version> TargetVersion parseAndMigrate(VersionedData data, int targetSchema) throws Exception {
    Version parsed = data.parseData(versions[data.schema]);
    return (TargetVersion)migrate(parsed, targetSchema);
  }

  private static Version migrate(Version version, int targetSchema) throws Exception {
    int currentSchema = version.getVersion();

    while (currentSchema != targetSchema) {
      try {
        if (version.getClass() != versions[currentSchema]) {
          throw new Exception(String.format("Data of version %d is contained in the wrong class type - aborting migration.", currentSchema));
        }

        Class<Migration<?, ?>> clazz;
        String method;
        if (currentSchema < targetSchema) {
          clazz = migrations[currentSchema];
          method = "up";
        } else {
          clazz = migrations[currentSchema - 1];
          method = "down";
        }
        Migration<?, ?> instance = clazz.getDeclaredConstructor().newInstance();
        version = (Version)clazz.getMethod(method, Version.class).invoke(instance, version);
        currentSchema = version.getVersion();
      } catch (Exception e) {
        throw new Exception(String.format("Unable to migrate from version %d to %d.", currentSchema, targetSchema), e);
      }
    }

    return version;
  }
}
