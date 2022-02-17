package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.ChatMateApiException;
import dev.rebel.chatmate.models.api.chat.GetChatResponse;
import dev.rebel.chatmate.models.api.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;

public class ChatEndpointProxy extends EndpointProxy {
  public ChatEndpointProxy(LogService logService, String basePath) {
    super(logService, basePath + "/chat");
  }

  public GetChatResponseData getChat(@Nullable Long since, @Nullable Integer limit) throws ConnectException, ChatMateApiException, Exception {
    long sinceTimestamp = since != null ? since : new Date().getTime();
    String limitParam = limit == null ? "" : String.format("&limit=%s", limit.toString());
    String url = String.format("?since=%d%s", sinceTimestamp, limitParam);

    return this.makeRequest(Method.GET, url, GetChatResponse.class);
  }
}
