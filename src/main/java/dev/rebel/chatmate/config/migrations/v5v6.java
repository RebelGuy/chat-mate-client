package dev.rebel.chatmate.config.migrations;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV4;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV5;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV6;
import dev.rebel.chatmate.util.Collections;

import java.util.HashMap;

public class v5v6 extends Migration<SerialisedConfigV5, SerialisedConfigV6> {
  @Override
  public SerialisedConfigV6 up(SerialisedConfigV5 data) {
    return new SerialisedConfigV6(data.soundEnabled,
        data.chatVerticalDisplacement,
        Config.CommandMessageChatVisibility.SHOWN.toString(),
        true,
        data.hudEnabled,
        data.showChatPlatformIcon,
        new SerialisedConfigV6.SerialisedSeparableHudElement(data.statusIndicator.enabled, data.statusIndicator.separatePlatforms, data.statusIndicator.showPlatformIcon, data.statusIndicator.platformIconPosition),
        new SerialisedConfigV6.SerialisedSeparableHudElement(data.viewerCount.enabled, data.viewerCount.separatePlatforms, data.viewerCount.showPlatformIcon, data.viewerCount.platformIconPosition),
        data.debugModeEnabled,
        data.lastGetChatResponse,
        data.lastGetChatMateEventsResponse,
        Collections.map(data.hudTransforms, SerialisedConfigV6.SerialisedHudElementTransform::new),
        new SerialisedConfigV6.SerialisedLoginInfo(null, null)
    );
  }

  @Override
  public SerialisedConfigV5 down(SerialisedConfigV6 data) {
    return new SerialisedConfigV5(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        false,
        false,
        data.showChatPlatformIcon,
        new SerialisedConfigV5.SerialisedSeparableHudElement(data.statusIndicator.enabled, data.statusIndicator.separatePlatforms, data.statusIndicator.showPlatformIcon, data.statusIndicator.platformIconPosition),
        new SerialisedConfigV5.SerialisedSeparableHudElement(data.viewerCount.enabled, data.viewerCount.separatePlatforms, data.viewerCount.showPlatformIcon, data.viewerCount.platformIconPosition),
        data.debugModeEnabled,
        data.lastGetChatResponse,
        data.lastGetChatMateEventsResponse,
        new HashMap<>()
    );
  }
}
