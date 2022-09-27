package dev.rebel.chatmate.services.util;

import dev.rebel.chatmate.services.util.TaskWrapper;

import java.util.Timer;

public class Debouncer {
  private final long debounceTime;
  private final Runnable callback;
  private Timer timer;

  public Debouncer(long debounceTime, Runnable callback) {
    this.debounceTime = debounceTime;
    this.callback = callback;
    this.timer = new Timer();
  }

  /** Runs the configured callback after the debounce time if `doDebounce` is not called again until then, otherwise rests the timer. */
  public void doDebounce() {
    this.timer.cancel();
    this.timer = new Timer();
    this.timer.schedule(new TaskWrapper(this.callback), this.debounceTime);
  }

  /** Cancel the current timer. */
  public void cancel() {
    this.timer.cancel();
  }
}
