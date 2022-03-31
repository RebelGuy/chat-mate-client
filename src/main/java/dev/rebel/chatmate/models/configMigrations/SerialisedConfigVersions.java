package dev.rebel.chatmate.models.configMigrations;

public class SerialisedConfigVersions {
  public static class SerialisedConfigV1 extends Version {
    public boolean soundEnabled;
    public int chatVerticalDisplacement;
    public boolean hudEnabled;
    public boolean showStatusIndicator;
    public boolean showLiveViewers;
    public boolean identifyPlatforms;

    public SerialisedConfigV1(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled, boolean showStatusIndicator, boolean showLiveViewers, boolean identifyPlatforms) {
      super(1);
      this.soundEnabled = soundEnabled;
      this.chatVerticalDisplacement = chatVerticalDisplacement;
      this.hudEnabled = hudEnabled;
      this.showStatusIndicator = showStatusIndicator;
      this.showLiveViewers = showLiveViewers;
      this.identifyPlatforms = identifyPlatforms;
    }
  }

  public static class SerialisedConfigV0 extends Version {
    public final boolean soundEnabled;
    public final int chatVerticalDisplacement;
    public final boolean hudEnabled;
    public final boolean showStatusIndicator;
    public final boolean showLiveViewers;

    public SerialisedConfigV0(boolean soundEnabled, int chatVerticalDisplacement, boolean hudEnabled, boolean showStatusIndicator, boolean showLiveViewers) {
      super(0);
      this.soundEnabled = soundEnabled;
      this.chatVerticalDisplacement = chatVerticalDisplacement;
      this.hudEnabled = hudEnabled;
      this.showStatusIndicator = showStatusIndicator;
      this.showLiveViewers = showLiveViewers;
    }
  }

  public abstract static class Version {
    // transient means that the property will not be serialised
    private final transient int schema;

    public Version(int schema) {
      this.schema = schema;
    }

    public int getVersion() {
      return this.schema;
    }
  }
}
