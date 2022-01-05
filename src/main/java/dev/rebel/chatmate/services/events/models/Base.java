package dev.rebel.chatmate.services.events.models;

public class Base {
  public static class EventIn {
    public Boolean isDataModified;

    public EventIn() { }

    public EventIn(boolean isDataModified) {
      this.isDataModified = isDataModified;
    }
  }

  public static class EventOut {

  }

  public static class EventOptions {

  }
}
