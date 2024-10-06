package dev.rebel.chatmate.events.models;

import dev.rebel.chatmate.api.publicObjects.user.PublicUser;

import java.util.Date;

public class LevelUpEventData {
  public final Date date;
  public final PublicUser user;
  public final int oldLevel;
  public final int newLevel;

  public LevelUpEventData(Date date, PublicUser user, int oldLevel, int newLevel) {
    this.date = date;
    this.user = user;
    this.oldLevel = oldLevel;
    this.newLevel = newLevel;
  }
}
