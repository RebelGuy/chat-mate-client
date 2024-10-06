package dev.rebel.chatmate.services;

import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.CursorService.CursorType;
import dev.rebel.chatmate.stores.StreamerApiStore;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ApiRequestService {
  private final CursorService cursorService;
  private final Config config;
  private final Object lock = new Object();
  private final Set<Consumer<Boolean>> listeners = Collections.newSetFromMap(new WeakHashMap<>());
  private int activeRequests = 0;
  private Supplier<List<PublicStreamerSummary>> getStreamers;

  public ApiRequestService(CursorService cursorService, Config config) {
    this.cursorService = cursorService;
    this.config = config;
    this.getStreamers = ArrayList::new;
  }

  public void setGetStreamers(Supplier<List<PublicStreamerSummary>> getStreamers) {
    this.getStreamers = getStreamers;
  }

  /** Call this when dispatching manual API requests, and run the provided Runnable once the request is complete. */
  public Runnable onNewRequest() {
    int cursorId = this.cursorService.toggleCursor(CursorType.WAIT, 0);

    // we must ensure that the listeners receive ordered data, else it may corrupt their state.
    synchronized (this.lock) {
      this.activeRequests++;
      this.updateListeners(1);
    }

    return () -> {
      this.cursorService.untoggleCursor(cursorId);
      synchronized (this.lock) {
        this.activeRequests--;
        this.updateListeners(-1);
      }
    };
  }

  /** Calls the listener when there is a change of activity (i.e. a transition between 0 active requests and 1 or more active requests). Stores a weak reference to the listener - no lambda allowed. */
  public void onActive(Consumer<Boolean> activeListener) {
    this.listeners.add(activeListener);
  }

  public @Nullable String getLoginToken() {
    return this.config.getLoginInfoEmitter().get().loginToken;
  }

  public @Nullable String getStreamer() {
    @Nullable String username = this.config.getLoginInfoEmitter().get().username;
    @Nullable List<PublicStreamerSummary> streamers = this.getStreamers.get();

    if (username == null || streamers == null) {
      return null;
    }

    return dev.rebel.chatmate.util.Collections.any(streamers, streamer -> Objects.equals(streamer.username, username)) ? username : null;
  }

  private void updateListeners(int delta) {
    // only notify listeners when the active state has changed
    if (this.activeRequests > 1 || this.activeRequests == 1 && delta == -1) {
      return;
    }

    boolean isActive = this.activeRequests == 1;
    this.listeners.forEach(l -> l.accept(isActive));
  }
}
