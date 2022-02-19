package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.ChatMateApiException;
import dev.rebel.chatmate.models.api.chatMate.GetEventsResponse;
import dev.rebel.chatmate.models.api.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.stores.ChatMateEndpointStore;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;

public class ChatMateEndpointProxy extends EndpointProxy {
  public ChatMateEndpointProxy(LogService logService, ChatMateEndpointStore chatMateEndpointStore, String basePath) {
    super(logService, chatMateEndpointStore, basePath + "/chatMate");
  }

  public GetStatusResponseData getStatus() throws ConnectException, ChatMateApiException, Exception {
    return this.makeRequest(Method.GET, "/status", GetStatusResponse.class);
  }

  public GetEventsResponseData getEvents(@Nullable Long sinceTimestamp) throws ConnectException, ChatMateApiException, Exception {
    if (sinceTimestamp == null) {
      sinceTimestamp = new Date().getTime();
    }
    String url = String.format("/events?since=%d", sinceTimestamp);
    return this.makeRequest(Method.GET, url, GetEventsResponse.class);
  }
}
