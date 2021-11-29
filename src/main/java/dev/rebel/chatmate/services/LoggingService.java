package dev.rebel.chatmate.services;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoggingService {
  private final @Nullable File file;

  public LoggingService(@Nullable String fileName, boolean debugOnly) throws Exception {
    if (fileName == null) {
      this.file = null;
    } else {
      String currentdir = System.getProperty("user.dir");
      currentdir = currentdir.replace("\\", "/");
      String dataDir = currentdir + "/mods/ChatMate";
      File dataDirFile = new File(dataDir);

      if (!fileName.startsWith("/")) fileName = "/" + fileName;
      this.file = new File(dataDir + fileName);

      if (!dataDirFile.exists()) dataDirFile.mkdir();
      if (this.file.exists()) this.file.delete();
      this.file.createNewFile();
    }
  }

  public void Log(Object... items) {
    DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    String timestamp = format.format(new Date());
    StringBuilder builder = new StringBuilder("[" + timestamp + "] ");
    for (Object obj: items) {
      String prefix = builder.length() > 0 ? " " : "";
      builder.append(prefix).append(obj.toString());
    }

    System.out.println(builder);

    if (this.file != null) {
      try {
        Writer writer = new OutputStreamWriter(new FileOutputStream(this.file, true), StandardCharsets.UTF_8);
        writer.write(builder + "\r\n");
        writer.close();
      } catch (Exception e) {
        System.out.println("Error logging line to the file: " + e.getMessage());
      }
    }
  }
}
