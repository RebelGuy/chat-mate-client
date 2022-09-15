package dev.rebel.chatmate.services;

import java.util.Date;

public class DateTimeService {
  public long now() {
    return new Date().getTime();
  }
}
