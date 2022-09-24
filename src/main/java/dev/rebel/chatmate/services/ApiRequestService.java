package dev.rebel.chatmate.services;

import dev.rebel.chatmate.services.CursorService.CursorType;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class ApiRequestService {
  private final CursorService cursorService;
  private final Object lock = new Object();
  private final Set<Consumer<Boolean>> listeners = Collections.newSetFromMap(new WeakHashMap<>());
  private int activeRequests = 0;

  public ApiRequestService(CursorService cursorService) {
    this.cursorService = cursorService;
  }

  /** Call this when dispatching manual API requests, and run the provided Runnable once the request is complete. */
  public Runnable onNewRequest() {
    int cursorId = this.cursorService.toggleCursor(CursorType.WAIT);

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

  private void updateListeners(int delta) {
    // only notify listeners when the active state has changed
    if (this.activeRequests > 1 || this.activeRequests == 1 && delta == -1) {
      return;
    }

    boolean isActive = this.activeRequests == 1;
    this.listeners.forEach(l -> l.accept(isActive));
  }
}
