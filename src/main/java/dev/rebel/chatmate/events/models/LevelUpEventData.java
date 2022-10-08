package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.events.models.LevelUpEventData.In;
import dev.rebel.chatmate.events.models.LevelUpEventData.Out;
import dev.rebel.chatmate.events.models.LevelUpEventData.Options;

import java.util.Date;

public class LevelUpEventData extends EventData<In, Out, Options> {
  public static class In extends EventIn {
    public final Date date;
    public final PublicUser user;
    public final int oldLevel;
    public final int newLevel;

    public In(Date date, PublicUser user, int oldLevel, int newLevel) {
      this.date = date;
      this.user = user;
      this.oldLevel = oldLevel;
      this.newLevel = newLevel;
    }
  }

  public static class Out extends EventOut {

  }

  public static class Options extends EventOptions {

  }
}
