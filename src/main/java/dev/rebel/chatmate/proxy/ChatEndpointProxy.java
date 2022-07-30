package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.api.chat.GetChatResponse;
import dev.rebel.chatmate.models.api.chat.GetChatResponse.GetChatResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.ApiRequestService;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.function.Consumer;

public class ChatEndpointProxy extends EndpointProxy {
  public ChatEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/chat");
  }

  public void getChatAsync(Consumer<GetChatResponseData> callback, @Nullable Consumer<Throwable> errorHandler, @Nullable Long since, @Nullable Integer limit) {
    long sinceTimestamp = since != null ? since : new Date().getTime();
    String limitParam = limit == null ? "" : String.format("&limit=%s", limit.toString());
    String url = String.format("?since=%d%s", sinceTimestamp, limitParam);

    this.makeRequestAsync(Method.GET, url, GetChatResponse.class, callback, errorHandler, false);
  }
}
