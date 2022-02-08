package dev.rebel.chatmate.models.configMigrations;

public class SerialisedConfigVersions {
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
    private final int schema;

    public Version(int schema) {
      this.schema = schema;
    }

    public int getVersion() {
      return this.schema;
    }
  }
}
