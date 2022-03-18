package dev.rebel.chatmate.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.rebel.chatmate.models.ChatMateApiException;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.stores.ChatMateEndpointStore;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EndpointProxy {
  private final LogService logService;
  private final ChatMateEndpointStore chatMateEndpointStore;
  private final String basePath;
  private final Gson gson;

  private int requestId = 0;

  public EndpointProxy(LogService logService, ChatMateEndpointStore chatMateEndpointStore, String basePath) {
    this.logService = logService;
    this.chatMateEndpointStore = chatMateEndpointStore;
    this.basePath = basePath;
    this.gson = new Gson();
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(method, path, null, returnClass, callback, errorHandler);
  }

  /** Error is one of the following types: ConnectException, ChatMateApiException, Exception. */
  public <Data, Res extends ApiResponseBase<Data>> void makeRequestAsync(Method method, String path, Object data, Class<Res> returnClass, Consumer<Data> callback, @Nullable Consumer<Throwable> errorHandler) {
    // we got there eventually.....
    CompletableFuture.supplyAsync(() -> {
      Runnable onComplete = this.chatMateEndpointStore.onNewRequest();
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
    }).thenAccept(callback);
  }

  public <Data, Res extends ApiResponseBase<Data>> Data makeRequest(Method method, String path, Class<Res> returnClass) throws ConnectException, ChatMateApiException, Exception {
    return this.makeRequest(method, path, returnClass, null);
  }

  public <Data, Res extends ApiResponseBase<Data>> Data makeRequest(Method method, String path, Class<Res> returnClass, Object data) throws ConnectException, ChatMateApiException, Exception {
    int id = ++this.requestId;
    this.logService.logApiRequest(this, id, method, this.basePath + path);

    Exception ex = null;
    String result;
    try {
      result = downloadString(method, path, data);
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
      Res parsed = this.parseResponse(result, returnClass);
      parsed.assertIntegrity();
      if (!parsed.success) {
        throw new ChatMateApiException(parsed.error);
      } else {
        return parsed.data;
      }
    } else {
      throw ex;
    }
  }

  private String downloadString(Method method, String path, Object data) throws Exception {
    URL url = new URL(this.basePath + path);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method.toString());

    if (method == Method.POST && data != null) {
      String json = this.gson.toJson(data);
      byte[] input = json.getBytes(StandardCharsets.UTF_8);

      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("charset", "utf-8");
      conn.setRequestProperty("Content-Length", String.valueOf(input.length));
      conn.setDoOutput(true); // allow data for be sent outwards
      try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
        wr.write(input);
      }
    }

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
      ChatMateApiException error = (ChatMateApiException)e;
      msg = error.apiResponseError.message;
      if (msg == null) {
        msg = error.apiResponseError.errorType;
      }
    } else {
      msg = "Something went wrong.";
    }

    return msg;
  }

  public enum Method { GET, POST }
}
