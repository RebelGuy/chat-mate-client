package dev.rebel.chatmate.models.configMigrations;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.SeparableHudElement.PlatformIconPosition;

public class SerialisedConfigV4 extends SerialisedConfigVersions.Version {
  public boolean soundEnabled;
  public int chatVerticalDisplacement;
  public boolean hudEnabled;
  public boolean showServerLogsHeartbeat;
  public boolean showServerLogsTimeSeries;
  public final boolean showChatPlatformIcon;
  public final SerialisedSeparableHudElement statusIndicator;
  public final SerialisedSeparableHudElement viewerCount;
  public final boolean debugModeEnabled;
  public final long lastGetChatResponse;
  public final long lastGetChatMateEventsResponse;

  public SerialisedConfigV4(boolean soundEnabled,
                            int chatVerticalDisplacement,
                            boolean hudEnabled,
                            boolean showServerLogsHeartbeat,
                            boolean showServerLogsTimeSeries,
                            boolean showChatPlatformIcon,
                            SerialisedSeparableHudElement statusIndicator,
                            SerialisedSeparableHudElement viewerCount,
                            boolean debugModeEnabled,
                            long lastGetChatResponse,
                            long lastGetChatMateEventsResponse) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
    this.showServerLogsHeartbeat = showServerLogsHeartbeat;
    this.showServerLogsTimeSeries = showServerLogsTimeSeries;
    this.showChatPlatformIcon = showChatPlatformIcon;
    this.statusIndicator = statusIndicator;
    this.viewerCount = viewerCount;
    this.debugModeEnabled = debugModeEnabled;
    this.lastGetChatResponse = lastGetChatResponse;
    this.lastGetChatMateEventsResponse = lastGetChatMateEventsResponse;
  }

  @Override
  public int getVersion() {
    return 4;
  }

  public static class SerialisedSeparableHudElement {
    public final boolean enabled;
    public final boolean separatePlatforms;
    public final boolean showPlatformIcon;
    public final String platformIconPosition;

    public SerialisedSeparableHudElement(boolean enabled, boolean separatePlatforms, boolean showPlatformIcon, String platformIconPosition) {
      this.enabled = enabled;
      this.separatePlatforms = separatePlatforms;
      this.showPlatformIcon = showPlatformIcon;
      this.platformIconPosition = platformIconPosition;
    }

    public SerialisedSeparableHudElement(Config.SeparableHudElement separableHudElement) {
      this(separableHudElement.enabled,
          separableHudElement.separatePlatforms,
          separableHudElement.showPlatformIcon,
          separableHudElement.platformIconPosition.toString().toLowerCase()
      );
    }

    public Config.SeparableHudElement deserialise() {
      return new Config.SeparableHudElement(
          this.enabled,
          this.separatePlatforms,
          this.showPlatformIcon,
          this.platformIconPosition == "left" ? PlatformIconPosition.LEFT
              : this.platformIconPosition == "top" ? PlatformIconPosition.TOP
              : this.platformIconPosition == "right" ? PlatformIconPosition.RIGHT
              : this.platformIconPosition == "bottom" ? PlatformIconPosition.BOTTOM
              : PlatformIconPosition.LEFT
      );
    }
  }
}
