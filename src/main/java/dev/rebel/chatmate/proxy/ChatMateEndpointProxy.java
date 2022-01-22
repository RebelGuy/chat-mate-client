package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.chatMate.GetStatusResponse;
import dev.rebel.chatmate.services.LoggingService;

import java.net.ConnectException;

public class ChatMateEndpointProxy extends EndpointProxy {
  public ChatMateEndpointProxy(LoggingService loggingService, String basePath) {
    super(loggingService, basePath + "/chatMate");
  }

  public GetStatusResponse getStatus() throws ConnectException, Exception {
    return this.makeRequest(Method.GET, "/status", GetStatusResponse.class);
  }
}
