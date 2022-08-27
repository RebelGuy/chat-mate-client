package dev.rebel.chatmate.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.rebel.chatmate.models.ChatMateApiException;
import dev.rebel.chatmate.models.HttpException;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.ApiRequestService;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.rebel.chatmate.services.util.Objects.ifClass;

public class EndpointProxy {
  private final LogService logService;
  private final ApiRequestService apiRequestService;
  private final String basePath;
  private final Gson gson;

  private int requestId = 0;

  public EndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    this.logService = logService;
    this.apiRequestService = apiRequestService;
    this.basePath = basePath;
    this.gson = new GsonBuilder()
        .serializeNulls()
        .create();

    hack_allowPatchRequests();
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(method, path, null, returnClass, callback, errorHandler, true);
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Object data, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(method, path, data, returnClass, callback, errorHandler, true);
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler, boolean notifyEndpointStore) {
    this.makeRequestAsync(method, path, null, returnClass, callback, errorHandler, notifyEndpointStore);
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Object data, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler, boolean notifyEndpointStore) {
    // we got there eventually.....
    CompletableFuture.supplyAsync(() -> {
      Runnable onComplete = notifyEndpointStore ? this.apiRequestService.onNewRequest() : () -> {};
      try {
        Data result = this.makeRequest(method, path, returnClass, data);
        onComplete.run();
        return result;

      } catch (Exception e) {
        onComplete.run();
        if (errorHandler != null) {
          errorHandler.accept(e);
        }
        return null;
      }
    }).thenAccept(res -> {
      if (res != null) {
        // if there is an exception here, it will bubble up
        callback.accept(res);
      }
    });
  }

  public <Data, Res extends ApiResponseBase<Data>> Data makeRequest(Method method, String path, Class<Res> returnClass) throws ConnectException, ChatMateApiException, HttpException, Exception {
    return this.makeRequest(method, path, returnClass, null);
  }

  public <Data, Res extends ApiResponseBase<Data>> Data makeRequest(Method method, String path, Class<Res> returnClass, Object data) throws ConnectException, ChatMateApiException, HttpException, Exception {
    int id = ++this.requestId;
    this.logService.logApiRequest(this, id, method, this.basePath + path);

    ApiResponse result;
    try {
      result = downloadString(method, path, data);
    } catch (ConnectException e) {
      String message = "Failed to connect to the server - is it running? " + e.getMessage();
      this.logService.logApiResponse(this, id, null, true, message);
      throw e;
    } catch (JsonSyntaxException e) {
      String message = "Failed to parse JSON response to " + returnClass.getSimpleName() + " - has the schema changed? " + e.getMessage();
      this.logService.logApiResponse(this, id, null, true, message);
      throw e;
    } catch (Exception e) {
      String message = "Failed to get response. " + e.getMessage();
      this.logService.logApiResponse(this, id, null, true, message);
      throw e;
    }

    this.logService.logApiResponse(this, id, result.statusCode, !result.success, result.responseBody);
    try {
      Res parsed = this.parseResponse(result.responseBody, returnClass);
      parsed.assertIntegrity();
      if (!parsed.success) {
        throw new ChatMateApiException(parsed.error);
      } else {
        return parsed.data;
      }
    } catch (ChatMateApiException e) {
      throw e;
    } catch (Exception e) {
      // errors reaching here are most likely due to a response with an unexpected format, e.g. 502 errors.
      throw new HttpException(e.getMessage(), result.statusCode, result.responseBody);
    }
  }

  private ApiResponse downloadString(Method method, String path, Object data) throws Exception {
    URL url = new URL(this.basePath + path);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod(method.toString());

    if (method != Method.GET && data != null) {
      String json = this.gson.toJson(data);
      byte[] input = json.getBytes(StandardCharsets.UTF_8);

      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("charset", "utf-8");
      conn.setRequestProperty("Content-Length", String.valueOf(input.length));
      conn.setDoOutput(true); // allow data to be sent outwards
      try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
        wr.write(input);
      }
    }

    @Nullable Integer statusCode = conn.getResponseCode();
    boolean success;
    InputStream stream;
    try {
      stream = conn.getInputStream();
      success = true;
    } catch (IOException e) {
      // responses with certain error codes don't have input streams, only error streams.
      // if this also fails we let it bubble up
      stream = conn.getErrorStream();
      success = false;
    }

    // the stream can be null if there is no response body
    if (stream == null) {
      return new ApiResponse(success, statusCode, "");
    }

    // https://stackoverflow.com/a/1826995
    // turns out default encoding is system/environment dependent if not set explicitly.
    InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
    StringBuilder result = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(streamReader)) {
      for (String line; (line = reader.readLine()) != null; ) {
        result.append(line);
      }
    }
    return new ApiResponse(success, statusCode, result.toString());
  }

  private <T extends ApiResponseBase<?>> T parseResponse(String response, Class<T> returnClass) throws Exception {
    T parsed = this.gson.fromJson(response, returnClass);
    if (parsed == null) {
      throw new Exception("Parsed response is null - is the JSON conversion implemented correctly?");
    }

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

  public static String getApiErrorMessage(Throwable e) {
    String msg;
    if (e instanceof ConnectException) {
      msg = "Unable to connect.";
    } else if (e instanceof ChatMateApiException) {
      ChatMateApiException error = (ChatMateApiException) e;
      msg = error.apiResponseError.message;
      if (msg == null) {
        msg = error.apiResponseError.errorType;
      }
    } else if (ifClass(HttpException.class, e, ex -> ex.statusCode != 200)) {
      msg = String.format("Something went wrong (code %d).", ((HttpException)e).statusCode);
    } else {
      msg = "Something went wrong.";
    }

    return msg;
  }

  /** For some reason, the nice devs over at Java didn't think the PATCH method was valid, so we have to add it via reflection.<br/>
   * Adapted from https://stackoverflow.com/a/46323891. */
  private static void hack_allowPatchRequests() {
    try {
      Field methodsField = HttpURLConnection.class.getDeclaredField("methods");
      Field modifiersField = Field.class.getDeclaredField("modifiers");

      modifiersField.setAccessible(true);
      modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);
      methodsField.setAccessible(true);

      // replace the `methods` static string array
      String[] methods = (String[])methodsField.get(null);
      String[] newMethods = new String[methods.length + 1];
      System.arraycopy(methods, 0, newMethods, 0, methods.length);
      newMethods[newMethods.length - 1] = "PATCH";
      methodsField.set(null, newMethods);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Unable to add PATCH request method.");
    }
  }

  private static class ApiResponse {
    public final boolean success;
    public final int statusCode;
    public final String responseBody;

    public ApiResponse(boolean success, @Nullable Integer statusCode, String responseBody) {
      this.success = success;
      this.statusCode = statusCode;
      this.responseBody = responseBody;
    }
  }

  public enum Method { GET, POST, PATCH, DELETE }
}
