package dev.rebel.chatmate.services;

import dev.rebel.chatmate.util.EnumHelpers;

import java.util.Date;

public class DateTimeService {
  public long now() {
    return new Date().getTime();
  }

  public long nowPlus(UnitOfTime unit, double amount) {
    return this.now() + (long)((double)this.unitOfTimeToMs(unit) * amount);
  }

  private long unitOfTimeToMs(UnitOfTime unit) {
    switch (unit) {
      case SECOND:
        return 1000L;
      case MINUTE:
        return 60_000L;
      case HOUR:
        return 3600_000L;
      case DAY:
        return 3600_000L * 24L;
      default:
        throw EnumHelpers.<UnitOfTime>assertUnreachable(unit);
    }
  }

  public enum UnitOfTime {
    SECOND, MINUTE, HOUR, DAY
  }
}
