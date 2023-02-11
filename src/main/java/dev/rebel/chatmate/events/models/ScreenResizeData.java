package dev.rebel.chatmate.events.models;

public class ScreenResizeData {
  private final int newScreenWidth;
  private final int newScreenHeight;

  public ScreenResizeData(int newScreenWidth, int newScreenHeight) {
    this.newScreenWidth = newScreenWidth;
    this.newScreenHeight = newScreenHeight;
  }
}
