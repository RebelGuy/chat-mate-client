package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.api.log.GetTimestampsResponse;
import dev.rebel.chatmate.models.api.log.GetTimestampsResponse.GetTimestampsResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.stores.ChatMateEndpointStore;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class LogEndpointProxy extends EndpointProxy {
  public LogEndpointProxy(LogService logService, ChatMateEndpointStore chatMateEndpointStore, String basePath) {
    super(logService, chatMateEndpointStore, basePath + "/log");
  }

  public void getTimestamps(Consumer<GetTimestampsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/timestamps", GetTimestampsResponse.class, callback, errorHandler, false);
  }
}
