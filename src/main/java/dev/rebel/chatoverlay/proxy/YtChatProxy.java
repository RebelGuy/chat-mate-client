package dev.rebel.chatoverlay.proxy;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.rebel.chatoverlay.models.chat.GetChatResponse;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class YtChatProxy {
  private final String basePath;
  private final Gson gson;

  public YtChatProxy(String basePath) {
    this.basePath = basePath;
    this.gson = new Gson();
  }

  public GetChatResponse GetChat(@Nullable Long since, @Nullable Integer limit) throws ConnectException, JsonSyntaxException, Exception {
    URL url = this.constructGetUrl(since, limit);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

    StringBuilder result = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(streamReader)) {
      for (String line; (line = reader.readLine()) != null; ) {
        result.append(line);
      }
    }
    String response = result.toString();
    GetChatResponse parsed = this.gson.fromJson(response, GetChatResponse.class);

    if (parsed.schema == null) {
      throw new Exception("Schema is null - is the JSON conversion is implemented correctly?");
    } else if (parsed.schema.intValue() != parsed.GetExpectedSchema().intValue()) {
      throw new Exception("Schema mismatch - expected " + parsed.GetExpectedSchema().toString() + " but received " + parsed.schema.toString());
    }

    return parsed;
  }

  private URL constructGetUrl(@Nullable Long since, @Nullable Integer limit) throws MalformedURLException {
    long sinceTimestamp = 0;
    if (since != null) {
      sinceTimestamp = since;
    }

    String limitParam = limit == null ? "" : String.format("&limit=%s", limit.toString());
    String url = String.format("%schat?since=%d%s", this.basePath, sinceTimestamp, limitParam);

    return new URL(url);
  }
}
