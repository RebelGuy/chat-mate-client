package dev.rebel.chatmate.util;

import java.util.TimerTask;

public class TaskWrapper extends TimerTask {
  private final Runnable runnable;

  public TaskWrapper(Runnable runnable) {
    this.runnable = runnable;
  }

  public void run() {
    this.runnable.run();
  }
}
