package dev.rebel.chatmate.services;

import com.google.gson.Gson;
import dev.rebel.chatmate.proxy.EndpointProxy.Method;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class LogService {
  private final FileService fileService;
  private final @Nullable String fileName;
  private final Gson gson;

  public LogService(FileService fileService, boolean debugOnly) throws Exception {
    this.fileService = fileService;

    DateFormat timeFormat = new SimpleDateFormat("yyyy_MM_dd");
    String timestamp = timeFormat.format(new Date());
    this.fileName = String.format("log_%s.log", timestamp);

    this.gson = new Gson();
  }

  public void logDebug(Object logger, Object... args) {
    this.log("DEBUG", logger, args);
  }

  public void logWarning(Object logger, Object... args) {
    this.log("WARNING", logger, args);
  }

  public void logError(Object logger, Object... args) {
    this.log("ERROR", logger, args);
  }

  public void logInfo(Object logger, Object... args) {
    this.log("INFO", logger, args);
  }

  public void logApiRequest(Object logger, int requestId, Method method, String url) {
    this.log("API", logger, String.format("%s request #%d dispatched to %s", method, requestId, url));
  }

  public void logApiResponse(Object logger, int requestId, boolean error, String response) {
    this.log("API", logger, String.format("Request #%d %s with response %s", requestId, error ? "failed" : "succeeded", response));
  }

  private void log(String type, Object logger, Object... args) {
    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    String timestamp = timeFormat.format(new Date());
    String loggerName = logger.getClass().getSimpleName();

    String prefix = String.format("%s %s > [%s] ", timestamp, type, loggerName);
    String body = String.join(" ", Arrays.stream(args).map(this::stringify).toArray(String[]::new));
    String message = prefix + body;

    System.out.println(message);
    if (this.fileName != null) {
      try {
        this.fileService.writeFile(this.fileName, message, true);
      } catch (Exception e) {
        System.out.println("Error logging line to the file: " + e.getMessage());
      }
    }
  }

  private String stringify(Object obj) {
    if (obj == null) {
      return "null";
    }

    Class<?> c = obj.getClass();
    if (c.isPrimitive() || c.equals(String.class)) {
      return obj.toString();
    } else {
      return this.gson.toJson(obj);
    }
  }
}
