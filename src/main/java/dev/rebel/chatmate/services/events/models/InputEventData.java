package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.InputEventData.In;
import dev.rebel.chatmate.services.events.models.InputEventData.Out;
import dev.rebel.chatmate.services.events.models.InputEventData.Options;

public class InputEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public In() {

    }
  }

  public static class Out extends EventOut {
    public boolean cancelled;

    public Out(boolean cancelled) {
      this.cancelled = cancelled;
    }
  }

  public static class Options extends EventOptions {
    public Options() {

    }
  }
}
