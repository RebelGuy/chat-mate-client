package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.ChatMateApiException;
import dev.rebel.chatmate.models.api.chatMate.GetEventsResponse;
import dev.rebel.chatmate.models.api.chatMate.GetEventsResponse.GetEventsResponseData;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse;
import dev.rebel.chatmate.models.api.chatMate.GetStatusResponse.GetStatusResponseData;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamRequest;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamResponse;
import dev.rebel.chatmate.models.api.chatMate.SetActiveLivestreamResponse.SetActiveLivestreamResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.stores.ChatMateEndpointStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.function.Consumer;

public class ChatMateEndpointProxy extends EndpointProxy {
  public ChatMateEndpointProxy(LogService logService, ChatMateEndpointStore chatMateEndpointStore, String basePath) {
    super(logService, chatMateEndpointStore, basePath + "/chatMate");
  }

  public void getStatusAsync(Consumer<GetStatusResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/status", GetStatusResponse.class, callback, errorHandler);
  }

  public void getEventsAsync(Consumer<GetEventsResponseData> callback, @Nullable Consumer<Throwable> errorHandler, @Nullable Long sinceTimestamp) {
    if (sinceTimestamp == null) {
      sinceTimestamp = new Date().getTime();
    }
    String url = String.format("/events?since=%d", sinceTimestamp);
    this.makeRequestAsync(Method.GET, url, GetEventsResponse.class, callback, errorHandler);
  }

  public void setActiveLivestreamAsync(@Nonnull SetActiveLivestreamRequest request, Consumer<SetActiveLivestreamResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.PATCH, "/livestream", request, SetActiveLivestreamResponse.class, callback, errorHandler);
  }
}
