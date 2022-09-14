package dev.rebel.chatmate.store;

import dev.rebel.chatmate.models.publicObjects.livestream.PublicLivestream;
import dev.rebel.chatmate.proxy.EndpointProxy;
import dev.rebel.chatmate.proxy.GetLivestreamsResponse;
import dev.rebel.chatmate.proxy.GetLivestreamsResponse.GetLivestreamsResponseData;
import dev.rebel.chatmate.proxy.LivestreamEndpointProxy;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LivestreamApiStore {
  private final LivestreamEndpointProxy livestreamEndpointProxy;

  private @Nullable List<PublicLivestream> livestreams;
  private @Nullable String error;
  private boolean loading;

  public LivestreamApiStore(LivestreamEndpointProxy livestreamEndpointProxy) {
    this.livestreamEndpointProxy = livestreamEndpointProxy;

    this.livestreams = null;
    this.error = null;
    this.loading = false;
  }

  /** Fetches livestreams from the server. */
  public void loadLivestreams(Consumer<List<PublicLivestream>> callback, Consumer<Throwable> errorHandler, boolean forceLoad) {
    if (this.livestreams != null && !forceLoad) {
      callback.accept(this.livestreams);
      return;
    } else if (this.loading) {
      callback.accept(new ArrayList<>());
      return;
    }

    this.loading = true;
    this.livestreamEndpointProxy.getLivestreams(res -> {
      this.livestreams = Collections.list(res.livestreams);
      this.error = null;
      this.loading = false;
      callback.accept(this.livestreams);
    }, err -> {
      this.livestreams = null;
      this.error = EndpointProxy.getApiErrorMessage(err);
      this.loading = false;
      errorHandler.accept(err);
    });
  }

  /** Gets loaded livestreams. */
  public @Nonnull List<PublicLivestream> getLivestreams() {
    if (this.livestreams == null) {
      if (!this.loading) {
        this.loadLivestreams(r -> {}, e -> {}, false);
      }
      return new ArrayList<>();
    } else {
      return this.livestreams;
    }
  }

  public @Nullable String getError() {
    return this.error;
  }
}
