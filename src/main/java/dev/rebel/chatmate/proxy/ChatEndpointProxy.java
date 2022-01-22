package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.chat.GetChatResponse;
import dev.rebel.chatmate.services.LoggingService;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;

public class ChatEndpointProxy extends EndpointProxy {
  public ChatEndpointProxy(LoggingService loggingService, String basePath) {
    super(loggingService, basePath + "/chat");
  }

  public GetChatResponse getChat(@Nullable Long since, @Nullable Integer limit) throws ConnectException, Exception {
    long sinceTimestamp = since != null ? since : new Date().getTime();
    String limitParam = limit == null ? "" : String.format("&limit=%s", limit.toString());
    String url = String.format("?since=%d%s", sinceTimestamp, limitParam);

    return this.makeRequest(Method.GET, url, GetChatResponse.class);
  }
}
