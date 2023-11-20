package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.proxy.LivestreamEndpointProxy;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.events.models.ConfigEventOptions;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LivestreamApiStore {
  private final LivestreamEndpointProxy livestreamEndpointProxy;

  private @Nullable CopyOnWriteArrayList<PublicLivestream> livestreams;
  private @Nullable String error;
  private boolean loading;

  public LivestreamApiStore(LivestreamEndpointProxy livestreamEndpointProxy, Config config) {
    this.livestreamEndpointProxy = livestreamEndpointProxy;

    this.livestreams = null;
    this.error = null;
    this.loading = false;

    config.getLoginInfoEmitter().onChange(_info -> this.clear(), new ConfigEventOptions<>(info -> info.loginToken == null));
  }

  public void clear() {
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
      this.livestreams = new CopyOnWriteArrayList<>(Collections.list(res.livestreams));
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
