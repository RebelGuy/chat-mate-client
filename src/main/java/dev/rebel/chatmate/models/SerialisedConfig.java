package dev.rebel.chatmate.models;

public class SerialisedConfig {
  public final boolean soundEnabled;
  public final int chatVerticalDisplacement;
  public final boolean hudEnabled;

  public SerialisedConfig(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
  }
}
