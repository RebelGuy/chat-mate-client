package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.proxy.GetLivestreamsResponse.GetLivestreamsResponseData;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class LivestreamEndpointProxy extends EndpointProxy {
  public LivestreamEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/livestream");
  }

  public void getLivestreams(Consumer<GetLivestreamsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/", GetLivestreamsResponse.class, callback, errorHandler, false);
  }
}
