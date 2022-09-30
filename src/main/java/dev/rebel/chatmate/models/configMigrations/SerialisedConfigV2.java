package dev.rebel.chatmate.models.configMigrations;

public class SerialisedConfigV2 extends SerialisedConfigVersions.Version {
  public boolean soundEnabled;
  public int chatVerticalDisplacement;
  public boolean hudEnabled;
  public boolean showStatusIndicator;
  public boolean showLiveViewers;
  public boolean showServerLogsHeartbeat;
  public boolean showServerLogsTimeSeries;
  public boolean identifyPlatforms;

  public SerialisedConfigV2(boolean soundEnabled,
                            int chatVerticalDisplacement,
                            boolean hudEnabled,
                            boolean showStatusIndicator,
                            boolean showLiveViewers,
                            boolean showServerLogsHeartbeat,
                            boolean showServerLogsTimeSeries,
                            boolean identifyPlatforms) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
    this.showStatusIndicator = showStatusIndicator;
    this.showLiveViewers = showLiveViewers;
    this.showServerLogsHeartbeat = showServerLogsHeartbeat;
    this.showServerLogsTimeSeries = showServerLogsTimeSeries;
    this.identifyPlatforms = identifyPlatforms;
  }

  @Override
  public int getVersion() {
    return 2;
  }
}
