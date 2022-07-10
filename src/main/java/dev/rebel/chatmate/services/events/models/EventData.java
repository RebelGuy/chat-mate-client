package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOptions;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;

public class EventData<In extends EventIn, Out extends EventOut, Options extends EventOptions> {
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

  public static class Empty extends EventData<EventIn, EventOut, EventOptions> { }
}
