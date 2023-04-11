package dev.rebel.chatmate.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.rebel.chatmate.api.proxy.EndpointProxy.Method;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.LogLevel;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.TextHelpers;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogService {
  private final FileService fileService;
  private final Gson gson;

  // these are only non-null until we have injected the Config instance.
  private @Nullable Config config;
  private @Nullable List<Args> args;

  public LogService(FileService fileService) throws Exception {
    this.fileService = fileService;
    this.config = null;
    this.args = new ArrayList<>();

    this.gson = new GsonBuilder()
        .serializeNulls()
        .create();
  }

  // naughty!
  public void injectConfig(Config config) {
    if (this.config != null || this.args == null) {
      throw new RuntimeException("Config has already been set!");
    }

    this.config = config;
    for (Args args : this.args) {
      this.log(args.logLevel, args.logger, args.args);
    }
    this.args = null;
  }

  public void logDebug(Object logger, Object... args) {
    this.log(LogLevel.DEBUG, logger, args);
  }

  public void logWarning(Object logger, Object... args) {
    this.log(LogLevel.WARNING, logger, args);
  }

  public void logError(Object logger, Object... args) {
    this.log(LogLevel.ERROR, logger, args);
  }

  public void logInfo(Object logger, Object... args) {
    this.log(LogLevel.INFO, logger, args);
  }

  public void logApiRequest(Object logger, int requestId, Method method, String url) {
    this.log(LogLevel.API, logger, String.format("%s request #%d dispatched to %s", method, requestId, url));
  }

  public void logApiResponse(Object logger, int requestId, @Nullable Integer statusCode, boolean error, String response) {
    this.log(LogLevel.API, logger, String.format("Request #%d %s%s with response %s", requestId, error ? "failed" : "succeeded", statusCode == null ? "" : String.format(" (code %d)", statusCode), response));
  }

  private void log(LogLevel logLevel, Object logger, Object... args) {
    if (this.config == null) {
      // defer logging until we know what to log
      assert this.args != null;
      this.args.add(new Args(logLevel, logger, args));
      return;
    } else if (Arrays.stream(this.config.getLogLevelsEmitter().get()).noneMatch(level -> level == logLevel)) {
      return;
    }

    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    String timestamp = timeFormat.format(new Date());
    String loggerName = logger.getClass().getSimpleName();

    String prefix = String.format("%s %s > [%s] ", timestamp, logLevel.toString(), loggerName);
    String body = String.join(" ", Arrays.stream(args).map(this::stringify).toArray(String[]::new));
    String message = prefix + body;

    System.out.println(message);
    try {
      this.fileService.writeFile(this.getLogFileName(), message, true);
    } catch (Exception e) {
      System.out.println("Error logging line to the file: " + e.getMessage());
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
        return this.maskString((String)obj);
      } else if (ClassUtils.isPrimitiveOrWrapper(obj.getClass())) {
        return obj.toString();
      } else {
        return this.gson.toJson(obj);
      }
    } catch (Exception e) {
      return String.format("[Unable to stringify exception object of type %s. Exception message: %s]", obj.getClass().getName(), e.getMessage());
    }
  }

  /** Assuming the string is in JSON format, redacts string values of properties that may hold sensitive information. */
  private String maskString(String string) {
    // e.g. "{\"password\":\"test1\\\"23456\"}" is turned into "{\"password\":\"<redacted>\"}"

    String originalString = string;
    try {
      Set<String> maskedProperties = new HashSet<>();
      maskedProperties.add("password");
      maskedProperties.add("loginToken");
      maskedProperties.add("X-Login-Token");

      for (String maskedProperty : maskedProperties) {
        String property = String.format("\"%s\":", maskedProperty.toLowerCase());
        ArrayList<Integer> occurrences = TextHelpers.getAllOccurrences(string.toLowerCase(), new TextHelpers.WordFilter(property), false);
        for (int occurrence : Collections.reverse(occurrences)) {
          // it is assumed that the masked value is a string
          int valueStart = occurrence + property.length();
          if (string.charAt(valueStart) != '"') {
            continue;
          }

          valueStart++;
          boolean escapeNextChar = false;
          String value = "";
          for (int i = valueStart; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c == '"' && !escapeNextChar) {
              break;
            }

            escapeNextChar = c == '\\';
            value += c;
          }

          string = string.substring(0, valueStart) + "<redacted>" + string.substring(valueStart + value.length());
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      return originalString;
    }

    return string;
  }

  private String getLogFileName() {
    DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd_HH");
    String timestamp = timeFormat.format(new Date());
    return String.format("log_%s.log", timestamp);
  }

  private static class Args {
    public final LogLevel logLevel;
    public final Object logger;
    public final Object[] args;

    private Args(LogLevel logLevel, Object logger, Object[] args) {
      this.logLevel = logLevel;
      this.logger = logger;
      this.args = args;
    }
  }
}
