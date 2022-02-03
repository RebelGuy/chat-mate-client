package dev.rebel.chatmate.models;

public class SerialisedConfig {
  public final boolean soundEnabled;
  public final int chatVerticalDisplacement;
  public final boolean hudEnabled;
  public final boolean showStatusIndicator;
  public final boolean showLiveViewers;

  public SerialisedConfig(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled, boolean showStatusIndicator, boolean showLiveViewers) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
    this.showStatusIndicator = showStatusIndicator;
    this.showLiveViewers = showLiveViewers;
  }
}
