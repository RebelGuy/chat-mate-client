package dev.rebel.chatoverlay.proxy;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rebel.chatoverlay.models.chat.GetChatResponse;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ChatProxy {
  private final String basePath;
  private final ObjectMapper objectMapper;

  public ChatProxy(String basePath) {
    this.basePath = basePath;
    this.objectMapper = new ObjectMapper();
  }

  public GetChatResponse GetChat(@Nullable Long since, @Nullable Integer limit) throws ConnectException, JsonProcessingException, Exception {
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
    GetChatResponse parsed = this.objectMapper.readValue(response, GetChatResponse.class);

    if (!parsed.schema.equals(parsed.GetExpectedSchema())) {
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
