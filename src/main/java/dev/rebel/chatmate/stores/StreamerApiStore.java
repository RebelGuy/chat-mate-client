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
  public void loadData(Consumer<StreamerState> callback, Consumer<Throwable> errorHandler, boolean forceLoad) {
    super.loadData(this::onFetchData, callback, errorHandler, forceLoad);
  }

  @Override
  public @Nullable StreamerState getData() {
    return super.getData(this::onFetchData);
  }

  private void onFetchData(Consumer<StreamerState> callback, Consumer<Throwable> errorHandler) {
    this.streamerEndpointProxy.getStreamersAsync(StreamerState::new, errorHandler);
  }

  public static class StreamerState {
    public final @Nullable CopyOnWriteArrayList<PublicStreamerSummary> streamers;

    public StreamerState(GetStreamersResponseData data) {
      this.streamers = Collections.safeList(data.streamers);
    }
  }
}
