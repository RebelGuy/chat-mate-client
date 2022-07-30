package dev.rebel.chatmate.services;

import dev.rebel.chatmate.services.CursorService.CursorType;

public class ApiRequestService {
  private final CursorService cursorService;

  public ApiRequestService(CursorService cursorService) {
    this.cursorService = cursorService;
  }

  /** Call this when dispatching manual API requests, and run the provided Runnable once the request is complete. */
  public Runnable onNewRequest() {
    int cursorId = this.cursorService.toggleCursor(CursorType.WAIT);
    return () -> this.cursorService.untoggleCursor(cursorId);
  }
}
