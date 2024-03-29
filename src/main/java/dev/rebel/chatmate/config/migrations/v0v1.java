package dev.rebel.chatmate.config.migrations;

import dev.rebel.chatmate.config.serialised.SerialisedConfigV0;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV1;

public class v0v1 extends Migration<SerialisedConfigV0, SerialisedConfigV1> {
  @Override
  public SerialisedConfigV1 up(SerialisedConfigV0 data) {
    return new SerialisedConfigV1(data.soundEnabled, data.chatVerticalDisplacement, data.hudEnabled, data.showStatusIndicator, data.showLiveViewers, false);
  }

  @Override
  public SerialisedConfigV0 down(SerialisedConfigV1 data) {
    return new SerialisedConfigV0(data.soundEnabled, data.chatVerticalDisplacement, data.hudEnabled, data.showStatusIndicator, data.showLiveViewers);
  }
}
