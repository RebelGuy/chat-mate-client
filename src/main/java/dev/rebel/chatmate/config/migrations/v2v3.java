package dev.rebel.chatmate.config.migrations;

import dev.rebel.chatmate.config.serialised.SerialisedConfigV2;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV3;

public class v2v3 extends Migration<SerialisedConfigV2, SerialisedConfigV3> {
  @Override
  public SerialisedConfigV3 up(SerialisedConfigV2 data) {
    return new SerialisedConfigV3(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showServerLogsHeartbeat,
        data.showServerLogsTimeSeries,
        data.identifyPlatforms,
        new SerialisedConfigV3.SerialisedSeparableHudElement(data.showStatusIndicator, data.identifyPlatforms, data.identifyPlatforms, "left"),
        new SerialisedConfigV3.SerialisedSeparableHudElement(data.showLiveViewers, data.identifyPlatforms, data.identifyPlatforms, "left"),
        false);
  }

  @Override
  public SerialisedConfigV2 down(SerialisedConfigV3 data) {
    return new SerialisedConfigV2(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.statusIndicator.enabled,
        data.viewerCount.enabled,
        data.showServerLogsHeartbeat,
        data.showServerLogsTimeSeries,
        data.statusIndicator.separatePlatforms || data.viewerCount.separatePlatforms);
  }
}
