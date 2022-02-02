package dev.rebel.chatmate.services;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingService {
  private final FileService fileService;
  private final @Nullable String fileName;

  public LoggingService(FileService fileService, @Nullable String fileName, boolean debugOnly) throws Exception {
    this.fileService = fileService;
    this.fileName = fileName;
  }

  public void log(Object... items) {
    DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    String timestamp = format.format(new Date());
    StringBuilder builder = new StringBuilder("[" + timestamp + "] ");
    for (Object obj: items) {
      String prefix = builder.length() > 0 ? " " : "";
      builder.append(prefix).append(obj.toString());
    }

    System.out.println(builder);

    if (this.fileName != null) {
      try {
        this.fileService.writeFile(this.fileName, builder.toString(), true);
      } catch (Exception e) {
        System.out.println("Error logging line to the file: " + e.getMessage());
      }
    }
  }
}
