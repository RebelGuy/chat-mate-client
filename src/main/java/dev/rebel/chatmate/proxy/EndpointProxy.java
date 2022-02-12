package dev.rebel.chatmate.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class EndpointProxy {
  private final LogService logService;
  private final String basePath;
  private final Gson gson;

  private int requestId = 0;

  public EndpointProxy(LogService logService, String basePath) {
    this.logService = logService;
    this.basePath = basePath;
    this.gson = new Gson();
  }

  public <T extends ApiResponseBase> void makeRequestAsync(Method method, String path, Class<T> returnClass, Consumer<T> callback, @Nullable Consumer<Throwable> errorHandler) {
    // we got there eventually.....
    CompletableFuture.supplyAsync(() -> {
      try {
        return this.makeRequest(method, path, returnClass);
      } catch (Exception e) {
        if (errorHandler != null) {
          errorHandler.accept(e);
        }
        return null;
      }
    }).thenAccept(callback);
  }

  public <T extends ApiResponseBase> T makeRequest(Method method, String path, Class<T> returnClass) throws ConnectException, Exception {
    int id = ++this.requestId;
    this.logService.logApiRequest(this, id, method, this.basePath + path);

    Exception ex = null;
    String result;
    try {
      result = downloadString(method, path);
    } catch (ConnectException e) {
      result = "Failed to connect to the server - is it running? " + e.getMessage();
      ex = e;
    } catch (JsonSyntaxException e) {
      result = "Failed to parse JSON response to " + returnClass.getSimpleName() + " - has the schema changed? " + e.getMessage();
      ex = e;
    } catch (Exception e) {
      result = "Failed to get response. " + e.getMessage();
      ex = e;
    }

    this.logService.logApiResponse(this, id, ex != null, result);
    if (ex == null) {
      return this.parseResponse(result, returnClass);
    } else {
      throw ex;
    }
  }

  private String downloadString(Method method, String path) throws Exception {
    URL url = new URL(this.basePath + path);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method.toString());

    // https://stackoverflow.com/a/1826995
    // turns out default encoding is system/environment dependent if not set explicitly.
    InputStreamReader streamReader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
    StringBuilder result = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(streamReader)) {
      for (String line; (line = reader.readLine()) != null; ) {
        result.append(line);
      }
    }
    return result.toString();
  }

  private <T extends ApiResponseBase> T parseResponse(String response, Class<T> returnClass) throws Exception {
    T parsed = this.gson.fromJson(response, returnClass);

    String error = null;
    if (parsed.schema == null) {
      error = "The response's `schema` property is null - is the JSON conversion implemented correctly?";
    } else if (parsed.schema.intValue() != parsed.GetExpectedSchema().intValue()) {
      error = "Schema mismatch - expected " + parsed.GetExpectedSchema() + " but received " + parsed.schema + " from server.";
    }

    if (error != null) {
      throw new Exception("SCHEMA ERROR for class " + returnClass.getSimpleName() + ": " + error);
    } else {
      return parsed;
    }
  }

  public enum Method { GET }
}
