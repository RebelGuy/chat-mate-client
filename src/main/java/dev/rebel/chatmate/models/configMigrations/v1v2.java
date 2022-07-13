package dev.rebel.chatmate.models.configMigrations;

import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV0;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV1;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV2;

public class v1v2 extends Migration<SerialisedConfigV1, SerialisedConfigV2> {
  public v1v2(SerialisedConfigV1 data) {
    super(data);
  }

  @Override
  public SerialisedConfigV2 up() {
    return new SerialisedConfigV2(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showStatusIndicator,
        data.showLiveViewers,
        true,
        false,
        data.identifyPlatforms);
  }
}
