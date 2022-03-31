package dev.rebel.chatmate.models.configMigrations;

import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV0;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV1;

public class v0v1 extends Migration<SerialisedConfigV0, SerialisedConfigV1> {
  public v0v1(SerialisedConfigV0 data) {
    super(data);
  }

  @Override
  public SerialisedConfigV1 up() {
    return new SerialisedConfigV1(data.soundEnabled, data.chatVerticalDisplacement, data.hudEnabled, data.showStatusIndicator, data.showLiveViewers, false);
  }
}
