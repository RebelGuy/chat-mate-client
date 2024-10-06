package dev.rebel.chatmate.config.migrations;

import dev.rebel.chatmate.config.serialised.SerialisedConfigV1;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV2;

public class v1v2 extends Migration<SerialisedConfigV1, SerialisedConfigV2> {
  @Override
  public SerialisedConfigV2 up(SerialisedConfigV1 data) {
    return new SerialisedConfigV2(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showStatusIndicator,
        data.showLiveViewers,
        true,
        false,
        data.identifyPlatforms);
  }

  @Override
  public SerialisedConfigV1 down(SerialisedConfigV2 data) {
    return new SerialisedConfigV1(data.soundEnabled, data.chatVerticalDisplacement, data.hudEnabled, data.showStatusIndicator, data.showLiveViewers, data.identifyPlatforms);
  }
}
