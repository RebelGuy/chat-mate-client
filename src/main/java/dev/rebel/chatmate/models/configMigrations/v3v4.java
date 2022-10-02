package dev.rebel.chatmate.models.configMigrations;

public class v3v4 extends Migration<SerialisedConfigV3, SerialisedConfigV4> {
  @Override
  public SerialisedConfigV4 up(SerialisedConfigV3 data) {
    return new SerialisedConfigV4(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showServerLogsHeartbeat,
        data.showServerLogsTimeSeries,
        data.showChatPlatformIcon,
        new SerialisedConfigV4.SerialisedSeparableHudElement(data.statusIndicator.enabled, data.statusIndicator.separatePlatforms, data.statusIndicator.showPlatformIcon, data.statusIndicator.platformIconPosition),
        new SerialisedConfigV4.SerialisedSeparableHudElement(data.viewerCount.enabled, data.viewerCount.separatePlatforms, data.viewerCount.showPlatformIcon, data.viewerCount.platformIconPosition),
        data.debugModeEnabled,
        0,
        0);
  }

  @Override
  public SerialisedConfigV3 down(SerialisedConfigV4 data) {
    return new SerialisedConfigV3(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showServerLogsHeartbeat,
        data.showServerLogsTimeSeries,
        data.showChatPlatformIcon,
        new SerialisedConfigV3.SerialisedSeparableHudElement(data.statusIndicator.enabled, data.statusIndicator.separatePlatforms, data.statusIndicator.showPlatformIcon, data.statusIndicator.platformIconPosition),
        new SerialisedConfigV3.SerialisedSeparableHudElement(data.viewerCount.enabled, data.viewerCount.separatePlatforms, data.viewerCount.showPlatformIcon, data.viewerCount.platformIconPosition),
        data.debugModeEnabled);
  }
}
