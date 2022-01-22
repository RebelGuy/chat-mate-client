package dev.rebel.chatmate.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.rebel.chatmate.services.LoggingService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EndpointProxy {
  private final LoggingService loggingService;
  private final String basePath;
  private final Gson gson;


  public EndpointProxy(LoggingService loggingService, String basePath) {
    this.loggingService = loggingService;
    this.basePath = basePath;
    this.gson = new Gson();
  }

  public <T extends ApiResponseBase> T makeRequest(Method method, String path, Class<T> returnClass) throws ConnectException, Exception {
    try {
      String response = downloadString(method, path);
      return this.parseResponse(response, returnClass);
    } catch (ConnectException e) {
      this.loggingService.log("[EndpointProxy] Failed to connect to the server - is it running? " + e.getMessage());
      throw e;
    } catch (JsonSyntaxException e) {
      this.loggingService.log("[EndpointProxy] Failed to parse JSON response to " + returnClass.getSimpleName() + " - has the schema changed? " + e.getMessage());
      throw e;
    } catch (Exception e) {
      this.loggingService.log("[EndpointProxy] Failed to get response. " + e.getMessage());
      throw e;
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