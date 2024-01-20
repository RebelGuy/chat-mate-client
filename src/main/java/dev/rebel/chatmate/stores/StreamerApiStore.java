package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.proxy.StreamerEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class StreamerApiStore {
  private final StreamerEndpointProxy streamerEndpointProxy;

  private @Nullable CopyOnWriteArrayList<PublicStreamerSummary> streamers;
  private @Nullable String error;
  private boolean loading;

  public StreamerApiStore(StreamerEndpointProxy streamerEndpointProxy) {
    this.streamerEndpointProxy = streamerEndpointProxy;

    this.streamers = null;
    this.error = null;
    this.loading = false;
  }

  public void clear() {
    this.streamers = null;
    this.error = null;
    this.loading = false;
  }

  public void loadStreamers(Consumer<List<PublicStreamerSummary>> callback, Consumer<Throwable> errorHandler, boolean forceLoad) {
    if (this.streamers != null && !forceLoad) {
      callback.accept(this.streamers);
      return;
    } else if (this.loading) {
      callback.accept(new ArrayList<>());
      return;
    }

    this.loading = true;
    this.streamerEndpointProxy.getStreamersAsync(res -> {
      this.streamers = new CopyOnWriteArrayList<>(Collections.list(res.streamers));
      this.error = null;
      this.loading = false;
      callback.accept(this.streamers);
    }, err -> {
      this.streamers = null;
      this.error = EndpointProxy.getApiErrorMessage(err);
      this.loading = false;
      errorHandler.accept(err);
    });
  }

  /** Gets loaded streamers. */
  public @Nonnull List<PublicStreamerSummary> getStreamers() {
    if (this.streamers == null) {
      if (!this.loading) {
        this.loadStreamers(r -> {}, e -> {}, false);
      }
      return new ArrayList<>();
    } else {
      return this.streamers;
    }
  }

  public @Nullable String getError() {
    return this.error;
  }
}
