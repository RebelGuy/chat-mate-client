package dev.rebel.chatmate.models;

import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.Version;
//import dev.rebel.chatmate.models.configMigrations.v0v1;
import dev.rebel.chatmate.services.FileService;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Timer;

public class ConfigPersistorService<SerialisedConfig extends Version> {
  private final static int CURRENT_SCHEMA = 0;

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

  public @Nullable
  SerialisedConfig load() {
    try {
      VersionedData data = this.fileService.readObjectFromFile(this.fileName, VersionedData.class);
      if (data.schema == 0) {
//        data = new v0v1(data.data).up();
      }

      if (data.schema == CURRENT_SCHEMA) {
        SerialisedConfig parsed = data.parseData(this.currentSerialisedVersion);
        this.logService.logInfo(this, "Parsed configuration for schema " + CURRENT_SCHEMA);
        return parsed;
      } else {
        throw new Exception("Failed to parse config (schema " + data.schema + ") to schema version " + CURRENT_SCHEMA);
      }
    } catch (Exception e) {
      this.logService.logError(this,"Unable to load configuration:", e.getMessage());
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
