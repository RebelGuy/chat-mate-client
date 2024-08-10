package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.chatMate.GetEventsResponse;
import dev.rebel.chatmate.api.models.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.api.models.chatMate.GetStatusResponse;
import dev.rebel.chatmate.api.models.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.api.models.chatMate.SetActiveLivestreamRequest;
import dev.rebel.chatmate.api.models.chatMate.SetActiveLivestreamResponse;
import dev.rebel.chatmate.api.models.chatMate.SetActiveLivestreamResponse.SetActiveLivestreamResponseData;
import dev.rebel.chatmate.api.models.streamer.GetPrimaryChannelsResponse;
import dev.rebel.chatmate.api.models.streamer.GetPrimaryChannelsResponse.GetPrimaryChannelsResponseData;
import dev.rebel.chatmate.api.models.streamer.GetStreamersResponse;
import dev.rebel.chatmate.api.models.streamer.GetStreamersResponse.GetStreamersResponseData;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.ApiRequestService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.function.Consumer;

public class StreamerEndpointProxy extends EndpointProxy {
  public StreamerEndpointProxy(LogService logService, ApiRequestService apiRequestService, Config config, String basePath) {
    super(logService, apiRequestService, config, basePath + "/streamer");
  }

  public void getStatusAsync(Consumer<GetStatusResponseData> callback, @Nullable Consumer<Throwable> errorHandler, boolean notifyEndpointStore) {
    this.makeRequestAsync(Method.GET, "/status", GetStatusResponse.class, callback, errorHandler, notifyEndpointStore);
  }

  public void getEventsAsync(Consumer<GetEventsResponseData> callback, @Nullable Consumer<Throwable> errorHandler, @Nullable Long sinceTimestamp) {
    if (sinceTimestamp == null) {
      sinceTimestamp = new Date().getTime();
    }
    String url = String.format("/events?since=%d", sinceTimestamp);
    this.makeRequestAsync(Method.GET, url, GetEventsResponse.class, callback, errorHandler, false);
  }

  public void setActiveLivestreamAsync(@Nonnull SetActiveLivestreamRequest request, Consumer<SetActiveLivestreamResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.PATCH, "/livestream", request, SetActiveLivestreamResponse.class, callback, errorHandler);
  }

  public void getStreamersAsync(Consumer<GetStreamersResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/", GetStreamersResponse.class, callback, errorHandler, false);
  }

  public void getPrimaryChannelsAsync(Consumer<GetPrimaryChannelsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/primaryChannels", GetPrimaryChannelsResponse.class, callback, errorHandler);
  }
}
