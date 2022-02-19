package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.services.util.TaskWrapper;

import javax.annotation.Nullable;
import java.util.Timer;

public class ChatMateEndpointStore {
  private int pendingRequests;
  private @Nullable
  Timer pendingRequestsResetTimer;

  public ChatMateEndpointStore () {
    this.pendingRequests = 0;
    this.pendingRequestsResetTimer = null;
  }

  /** Call this when dispatching manual API requests, and run the provided Runnable once the request is complete. */
  public Runnable onNewRequest() {
    this.pendingRequests = Math.max(0, this.pendingRequests);

    // in case something goes wrong or the caller forgets to call the Runnable, add a timer
    // that will automatically reset the waiting requests
    if (this.pendingRequestsResetTimer != null) {
      this.pendingRequestsResetTimer.cancel();
      this.pendingRequestsResetTimer = null;
    }
    this.pendingRequestsResetTimer = new Timer();
    this.pendingRequestsResetTimer.schedule(new TaskWrapper(() -> this.pendingRequests = 0), 10000);

    this.pendingRequests++;
    return () -> this.pendingRequests--;
  }

  public boolean isWaitingForResponse() {
    return this.pendingRequests > 0;
  }
}
