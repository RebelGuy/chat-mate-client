package dev.rebel.chatmate.config.serialised;

public class SerialisedConfigV0 extends SerialisedConfigVersions.Version {
  public final boolean soundEnabled;
  public final int chatVerticalDisplacement;
  public final boolean hudEnabled;
  public final boolean showStatusIndicator;
  public final boolean showLiveViewers;

  public SerialisedConfigV0(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled, boolean showStatusIndicator, boolean showLiveViewers) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
    this.showStatusIndicator = showStatusIndicator;
    this.showLiveViewers = showLiveViewers;
  }

  @Override
  public int getVersion() {
    return 0;
  }
}
