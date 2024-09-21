package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.util.Collections;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StreamerApiStore extends ApiStore<PublicStreamerSummary> {
  private final StreamerEndpointProxy streamerEndpointProxy;

  public StreamerApiStore(StreamerEndpointProxy streamerEndpointProxy, Config config) {
    super(config, false);

    this.streamerEndpointProxy = streamerEndpointProxy;
  }

  @Override
  protected void onFetchData(Consumer<List<PublicStreamerSummary>> onData, Consumer<Throwable> onError, boolean isActiveRequest) {
    this.streamerEndpointProxy.getStreamersAsync(res -> onData.accept(Collections.list(res.streamers)), onError, isActiveRequest);
  }

  @Override
  protected boolean onMatchItems(PublicStreamerSummary a, PublicStreamerSummary b) {
    return Objects.equals(a.username, b.username);
  }
}
