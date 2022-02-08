package dev.rebel.chatmate.models;

import dev.rebel.chatmate.services.FileService;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Timer;

public class ConfigPersistorService {
  private final LogService logService;
  private final FileService fileService;
  private final String fileName;
  private @Nullable Timer timer;
  private final int debounceTime = 500;

  public ConfigPersistorService(LogService logService, FileService fileService) {
    this.logService = logService;
    this.fileService = fileService;
    this.fileName = "config.json";
    this.timer = null;
  }

  public @Nullable SerialisedConfig load() {
    try {
      return this.fileService.readObjectFromFile(this.fileName, SerialisedConfig.class);
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
    this.timer.schedule(new TaskWrapper(() -> this.onSave(serialised)), this.debounceTime);
  }

  private void onSave(SerialisedConfig serialised) {
    try {
      this.fileService.writeObjectToFile(this.fileName, serialised);
    } catch (Exception e) {
      this.logService.logError(this,"Unable to save configuration:", e.getMessage());
    }
  }
}
