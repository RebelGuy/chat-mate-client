package dev.rebel.chatmate.config;

import dev.rebel.chatmate.config.migrations.Migration;
import dev.rebel.chatmate.config.serialised.SerialisedConfigVersions.Version;
import dev.rebel.chatmate.services.FileService;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Timer;

public class ConfigPersistorService<SerialisedConfig extends Version> {
  private final static int CURRENT_SCHEMA = 6;

  private final Class<SerialisedConfig> currentSerialisedVersion;
  private final LogService logService;
  private final FileService fileService;
  private final String fileName;
  private @Nullable Timer timer;
  private final int debounceTime = 500;

  public ConfigPersistorService(Class<SerialisedConfig> currentSerialisedVersion, LogService logService, FileService fileService) {
    this.currentSerialisedVersion = currentSerialisedVersion;
    this.logService = logService;
    this.fileService = fileService;
    this.fileName = "config.json";
    this.timer = null;
  }

  public @Nullable SerialisedConfig load() {
    try {
      VersionedData data = this.fileService.readObjectFromFile(this.fileName, VersionedData.class);
      this.logService.logInfo(this, "Read configuration file for schema", data.schema);
      SerialisedConfig parsed = Migration.parseAndMigrate(data, CURRENT_SCHEMA);

      if (parsed.getVersion() == CURRENT_SCHEMA) {
        this.logService.logInfo(this, "Parsed and migrated configuration to schema " + CURRENT_SCHEMA);
        return parsed;
      } else {
        this.logService.logError(this, "Failed to parse config (schema " + data.schema + ") to schema version " + CURRENT_SCHEMA);
        return null;
      }
    } catch (Exception e) {
      this.logService.logError(this, "Unable to load or migrate configuration:", e.getMessage(), e.getCause());
      return null;
    }
  }

  public void save(SerialisedConfig serialised) {
    if (this.timer != null) {
      this.timer.cancel();
    }

    this.timer = new Timer();
    VersionedData data = new VersionedData(serialised.getVersion(), serialised);
    this.timer.schedule(new TaskWrapper(() -> this.onSave(data)), this.debounceTime);
  }

  private void onSave(VersionedData data) {
    try {
      this.fileService.writeObjectToFile(this.fileName, data);
    } catch (Exception e) {
      this.logService.logError(this,"Unable to save configuration:", e.getMessage());
    }
  }
}
