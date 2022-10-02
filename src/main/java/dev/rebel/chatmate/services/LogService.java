package dev.rebel.chatmate.services;

import com.google.gson.Gson;
import dev.rebel.chatmate.api.proxy.EndpointProxy.Method;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

  public void logApiResponse(Object logger, int requestId, @Nullable Integer statusCode, boolean error, String response) {
    this.log("API", logger, String.format("Request #%d %s%s with response %s", requestId, error ? "failed" : "succeeded", statusCode == null ? "" : String.format(" (code %d)", statusCode), response));
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
    try {
      if (obj == null) {
        return "null";
      } else if (obj instanceof Exception) {
        Exception e = (Exception) obj;
        return String.format("\n---EXCEPTION LOG START\nEncountered error of type %s. Error message: %s\n%s\n---EXCEPTION LOG END\n",
            e.getClass().getSimpleName(),
            ExceptionUtils.getMessage(e),
            ExceptionUtils.getStackTrace(e));
      } else if (obj instanceof String) {
        return (String) obj;
      } else if (ClassUtils.isPrimitiveOrWrapper(obj.getClass())) {
        return obj.toString();
      } else {
        return this.gson.toJson(obj);
      }
    } catch (Exception e) {
      return String.format("[Unable to stringify exception object of type %s. Exception message: %s]", obj.getClass().getName(), e.getMessage());
    }
  }
}
