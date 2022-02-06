package dev.rebel.chatmate.services.events.models;

import dev.rebel.chatmate.services.events.models.LevelUpEventData.In;
import dev.rebel.chatmate.services.events.models.LevelUpEventData.Out;
import dev.rebel.chatmate.services.events.models.LevelUpEventData.Options;

import java.util.Date;

public class LevelUpEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final Date date;
    public final String channelName;
    public final int oldLevel;
    public final int newLevel;

    public In(Date date, String channelName, int oldLevel, int newLevel) {
      this.date = date;
      this.channelName = channelName;
      this.oldLevel = oldLevel;
      this.newLevel = newLevel;
    }
  }

  public static class Out extends EventOut {

  }

  public static class Options extends EventOptions {

  }
}