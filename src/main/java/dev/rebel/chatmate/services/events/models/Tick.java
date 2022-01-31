package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.Tick.In;
import dev.rebel.chatmate.services.events.models.Tick.Out;
import dev.rebel.chatmate.services.events.models.Tick.Options;

public class Tick extends EventData<In, Out, Options> {
  public static class In extends EventIn {
  }

  public static class Out extends EventOut {
  }

  public static class Options extends EventOptions {
  }
}
