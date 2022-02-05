package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.chatMate.GetEventsResponse;
import dev.rebel.chatmate.models.chatMate.GetStatusResponse;
import dev.rebel.chatmate.services.LoggingService;

import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;

public class ChatMateEndpointProxy extends EndpointProxy {
  public ChatMateEndpointProxy(LoggingService loggingService, String basePath) {
    super(loggingService, basePath + "/chatMate");
  }

  public GetStatusResponse getStatus() throws ConnectException, Exception {
    return this.makeRequest(Method.GET, "/status", GetStatusResponse.class);
  }

  public GetEventsResponse getEvents(@Nullable Long sinceTimestamp) throws ConnectException, Exception {
    if (sinceTimestamp == null) {
      sinceTimestamp = new Date().getTime();
    }
    String url = String.format("/events?since=%d", sinceTimestamp);
    return this.makeRequest(Method.GET, url, GetEventsResponse.class);
  }
}
