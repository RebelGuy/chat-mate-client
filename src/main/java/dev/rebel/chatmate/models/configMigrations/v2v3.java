package dev.rebel.chatmate.models.configMigrations;

public class v2v3 extends Migration<SerialisedConfigV2, SerialisedConfigV3> {
  @Override
  public SerialisedConfigV3 up(SerialisedConfigV2 data) {
    return new SerialisedConfigV3(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showStatusIndicator,
        data.showLiveViewers,
        true,
        false,
        data.identifyPlatforms,
        true);
  }

  @Override
  public SerialisedConfigV2 down(SerialisedConfigV3 data) {
    return new SerialisedConfigV2(data.soundEnabled,
        data.chatVerticalDisplacement,
        data.hudEnabled,
        data.showStatusIndicator,
        data.showLiveViewers,
        data.identifyPlatforms,
        data.showServerLogsTimeSeries,
        data.identifyPlatforms);
  }
}
