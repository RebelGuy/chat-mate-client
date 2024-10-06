package dev.rebel.chatmate.config.migrations;

import dev.rebel.chatmate.config.serialised.SerialisedConfigV4;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV5;
import dev.rebel.chatmate.util.Collections;

import java.util.HashMap;

public class v4v5 extends Migration<SerialisedConfigV4, SerialisedConfigV5> {
  @Override
  public SerialisedConfigV5 up(SerialisedConfigV4 data) {
    return new SerialisedConfigV5(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showServerLogsHeartbeat,
        data.showServerLogsTimeSeries,
        data.showChatPlatformIcon,
        new SerialisedConfigV5.SerialisedSeparableHudElement(data.statusIndicator.enabled, data.statusIndicator.separatePlatforms, data.statusIndicator.showPlatformIcon, data.statusIndicator.platformIconPosition),
        new SerialisedConfigV5.SerialisedSeparableHudElement(data.viewerCount.enabled, data.viewerCount.separatePlatforms, data.viewerCount.showPlatformIcon, data.viewerCount.platformIconPosition),
        data.debugModeEnabled,
        data.lastGetChatResponse,
        data.lastGetChatMateEventsResponse,
        new HashMap<>() // reset HUD transforms (it's not possible to accurately migrate these here)
    );
  }

  @Override
  public SerialisedConfigV4 down(SerialisedConfigV5 data) {
    return new SerialisedConfigV4(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showServerLogsHeartbeat,
        data.showServerLogsTimeSeries,
        data.showChatPlatformIcon,
        new SerialisedConfigV4.SerialisedSeparableHudElement(data.statusIndicator.enabled, data.statusIndicator.separatePlatforms, data.statusIndicator.showPlatformIcon, data.statusIndicator.platformIconPosition),
        new SerialisedConfigV4.SerialisedSeparableHudElement(data.viewerCount.enabled, data.viewerCount.separatePlatforms, data.viewerCount.showPlatformIcon, data.viewerCount.platformIconPosition),
        data.debugModeEnabled,
        data.lastGetChatResponse,
        data.lastGetChatMateEventsResponse,
        new HashMap<>()
    );
  }
}
