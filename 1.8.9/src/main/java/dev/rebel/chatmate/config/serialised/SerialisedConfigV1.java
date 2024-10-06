package dev.rebel.chatmate.config.serialised;

public class SerialisedConfigV1 extends SerialisedConfigVersions.Version {
  public boolean soundEnabled;
  public int chatVerticalDisplacement;
  public boolean hudEnabled;
  public boolean showStatusIndicator;
  public boolean showLiveViewers;
  public boolean identifyPlatforms;

  public SerialisedConfigV1(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled, boolean showStatusIndicator, boolean showLiveViewers, boolean identifyPlatforms) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
    this.showStatusIndicator = showStatusIndicator;
    this.showLiveViewers = showLiveViewers;
    this.identifyPlatforms = identifyPlatforms;
  }

  @Override
  public int getVersion() {
    return 1;
  }
}
