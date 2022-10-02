package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.events.models.NewTwitchFollowerEventData.In;
import dev.rebel.chatmate.events.models.NewTwitchFollowerEventData.Options;
import dev.rebel.chatmate.events.models.NewTwitchFollowerEventData.Out;

import java.util.Date;

public class NewTwitchFollowerEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final Date date;
    public final String displayName;

    public In(Date date, String displayName) {
      this.date = date;
      this.displayName = displayName;
    }
  }

  public static class Out extends EventOut {

  }

  public static class Options extends EventOptions {

  }
}
