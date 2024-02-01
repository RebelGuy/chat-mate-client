package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.models.streamer.GetStreamersResponse.GetStreamersResponseData;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class StreamerApiStore extends ApiStore<StreamerApiStore.StreamerState> {
  private final StreamerEndpointProxy streamerEndpointProxy;

  public StreamerApiStore(StreamerEndpointProxy streamerEndpointProxy) {
    this.streamerEndpointProxy = streamerEndpointProxy;
  }

  @Override
  public void loadData(@Nullable Consumer<StreamerState> callback, @Nullable Consumer<Throwable> errorHandler, boolean forceLoad) {
    super.loadData(this::onFetchData, callback, errorHandler, forceLoad);
  }

  @Override
  public @Nullable StreamerState getData() {
    return super.getData(this::onFetchData);
  }

  @Override
  public void retry() {
    super.loadData(this::onFetchData, null, null, true);
  }

  private void onFetchData(@Nullable Consumer<StreamerState> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.streamerEndpointProxy.getStreamersAsync(res -> {
      if (callback != null) {
        callback.accept(new StreamerState(res));
      }
    }, errorHandler);
  }

  public static class StreamerState {
    public final @Nullable CopyOnWriteArrayList<PublicStreamerSummary> streamers;

    public StreamerState(GetStreamersResponseData data) {
      this.streamers = Collections.safeList(data.streamers);
    }
  }
}
