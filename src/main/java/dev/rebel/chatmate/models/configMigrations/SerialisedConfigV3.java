package dev.rebel.chatmate.models.configMigrations;

public class SerialisedConfigV3 extends SerialisedConfigVersions.Version {
  public boolean soundEnabled;
  public int chatVerticalDisplacement;
  public boolean hudEnabled;
  public boolean showStatusIndicator;
  public boolean showLiveViewers;
  public boolean showServerLogsHeartbeat;
  public boolean showServerLogsTimeSeries;
  public boolean identifyPlatforms;
  public final boolean showChatPlatformIcon;

  public SerialisedConfigV3(boolean soundEnabled,
                            int chatVerticalDisplacement,
                            boolean hudEnabled,
                            boolean showStatusIndicator,
                            boolean showLiveViewers,
                            boolean showServerLogsHeartbeat,
                            boolean showServerLogsTimeSeries,
                            boolean identifyPlatforms,
                            boolean showChatPlatformIcon) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
    this.showStatusIndicator = showStatusIndicator;
    this.showLiveViewers = showLiveViewers;
    this.showServerLogsHeartbeat = showServerLogsHeartbeat;
    this.showServerLogsTimeSeries = showServerLogsTimeSeries;
    this.identifyPlatforms = identifyPlatforms;
    this.showChatPlatformIcon = showChatPlatformIcon;
  }

  @Override
  public int getVersion() {
    return 3;
  }
}
