package dev.rebel.chatmate.events.models;

import java.util.Date;

public class NewTwitchFollowerEventData {
  public final Date date;
  public final String displayName;

  public NewTwitchFollowerEventData(Date date, String displayName) {
    this.date = date;
    this.displayName = displayName;
  }
}
