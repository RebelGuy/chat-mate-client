package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.publicObjects.livestream.PublicAggregateLivestream;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.proxy.LivestreamEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.livestream.PublicLivestream;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.events.models.ConfigEventOptions;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class LivestreamApiStore extends ApiStore<PublicAggregateLivestream> {
  private final LivestreamEndpointProxy livestreamEndpointProxy;

  public LivestreamApiStore(LivestreamEndpointProxy livestreamEndpointProxy, Config config) {
    super(config, true);

    this.livestreamEndpointProxy = livestreamEndpointProxy;
  }

  @Override
  protected void onFetchData(Consumer<List<PublicAggregateLivestream>> onData, Consumer<Throwable> onError, boolean isActiveRequest) {
    this.livestreamEndpointProxy.getLivestreams(
        res -> onData.accept(Collections.list(res.aggregateLivestreams)),
        onError,
        isActiveRequest
    );
  }

  @Override
  protected boolean onMatchItems(PublicAggregateLivestream a, PublicAggregateLivestream b) {
    if (!Objects.equals(a.startTime, b.startTime) || !Objects.equals(a.endTime, b.endTime) || a.livestreams.length != b.livestreams.length) {
      return false;
    }

    for (int i = 0; i < a.livestreams.length; i++) {
      PublicLivestream livestreamA = a.livestreams[i];
      PublicLivestream livestreamB = b.livestreams[i];

      if (!Objects.equals(livestreamA.id, livestreamB.id) ||
          !Objects.equals(livestreamA.startTime, livestreamB.startTime) ||
          !Objects.equals(livestreamA.endTime, livestreamB.endTime)) {
        return false;
      }
    }

    return true;
  }
}
