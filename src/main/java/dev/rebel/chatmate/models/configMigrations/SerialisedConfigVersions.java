package dev.rebel.chatmate.models.configMigrations;

public class SerialisedConfigVersions {
  public static class SerialisedConfigV2 extends Version {
    public boolean soundEnabled;
    public int chatVerticalDisplacement;
    public boolean hudEnabled;
    public boolean showStatusIndicator;
    public boolean showLiveViewers;
    public boolean showServerLogsHeartbeat;
    public boolean showServerLogsTimeSeries;
    public boolean identifyPlatforms;

    public SerialisedConfigV2(boolean soundEnabled,
                              int chatVerticalDisplacement,
                              boolean hudEnabled,
                              boolean showStatusIndicator,
                              boolean showLiveViewers,
                              boolean showServerLogsHeartbeat,
                              boolean showServerLogsTimeSeries,
                              boolean identifyPlatforms) {
      this.soundEnabled = soundEnabled;
      this.chatVerticalDisplacement = chatVerticalDisplacement;
      this.hudEnabled = hudEnabled;
      this.showStatusIndicator = showStatusIndicator;
      this.showLiveViewers = showLiveViewers;
      this.showServerLogsHeartbeat = showServerLogsHeartbeat;
      this.showServerLogsTimeSeries = showServerLogsTimeSeries;
      this.identifyPlatforms = identifyPlatforms;
    }

    @Override
    public int getVersion() {
      return 2;
    }
  }

  public static class SerialisedConfigV1 extends Version {
    public boolean soundEnabled;
    public int chatVerticalDisplacement;
    public boolean hudEnabled;
    public boolean showStatusIndicator;
    public boolean showLiveViewers;
    public boolean identifyPlatforms;

    public SerialisedConfigV1(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled, boolean showStatusIndicator, boolean showLiveViewers, boolean identifyPlatforms) {
      this.soundEnabled = soundEnabled;
      this.chatVerticalDisplacement = chatVerticalDisplacement;
      this.hudEnabled = hudEnabled;
      this.showStatusIndicator = showStatusIndicator;
      this.showLiveViewers = showLiveViewers;
      this.identifyPlatforms = identifyPlatforms;
    }

    @Override
    public int getVersion() {
      return 1;
    }
  }

  public static class SerialisedConfigV0 extends Version {
    public final boolean soundEnabled;
    public final int chatVerticalDisplacement;
    public final boolean hudEnabled;
    public final boolean showStatusIndicator;
    public final boolean showLiveViewers;

    public SerialisedConfigV0(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled, boolean showStatusIndicator, boolean showLiveViewers) {
      this.soundEnabled = soundEnabled;
      this.chatVerticalDisplacement = chatVerticalDisplacement;
      this.hudEnabled = hudEnabled;
      this.showStatusIndicator = showStatusIndicator;
      this.showLiveViewers = showLiveViewers;
    }

    @Override
    public int getVersion() {
      return 0;
    }
  }

  public abstract static class Version {
    public abstract int getVersion();
  }
}
