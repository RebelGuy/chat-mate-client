package dev.rebel.chatmate.models.configMigrations;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.SeparableHudElement.PlatformIconPosition;

import javax.annotation.Nullable;

public class SerialisedConfigV3 extends SerialisedConfigVersions.Version {
  public boolean soundEnabled;
  public int chatVerticalDisplacement;
  public boolean hudEnabled;
  public boolean showServerLogsHeartbeat;
  public boolean showServerLogsTimeSeries;
  public final boolean showChatPlatformIcon;
  public final SerialisedSeparableHudElement statusIndicator;
  public final SerialisedSeparableHudElement viewerCount;

  public SerialisedConfigV3(boolean soundEnabled,
                            int chatVerticalDisplacement,
                            boolean hudEnabled,
                            boolean showServerLogsHeartbeat,
                            boolean showServerLogsTimeSeries,
                            boolean showChatPlatformIcon,
                            SerialisedSeparableHudElement statusIndicator,
                            SerialisedSeparableHudElement viewerCount) {
    this.soundEnabled = soundEnabled;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.hudEnabled = hudEnabled;
    this.showServerLogsHeartbeat = showServerLogsHeartbeat;
    this.showServerLogsTimeSeries = showServerLogsTimeSeries;
    this.showChatPlatformIcon = showChatPlatformIcon;
    this.statusIndicator = statusIndicator;
    this.viewerCount = viewerCount;
  }

  @Override
  public int getVersion() {
    return 3;
  }

  public static class SerialisedSeparableHudElement {
    public final boolean enabled;
    public final boolean separatePlatforms;
    public final @Nullable String platformIconPosition;

    public SerialisedSeparableHudElement(boolean enabled, boolean separatePlatforms, String platformIconPosition) {
      this.enabled = enabled;
      this.separatePlatforms = separatePlatforms;
      this.platformIconPosition = platformIconPosition;
    }

    public SerialisedSeparableHudElement(Config.SeparableHudElement separableHudElement) {
      this(separableHudElement.enabled,
          separableHudElement.separatePlatforms,
          separableHudElement.platformIconPosition == null ? null : separableHudElement.platformIconPosition.toString().toLowerCase()
      );
    }

    public Config.SeparableHudElement deserialise() {
      return new Config.SeparableHudElement(
          this.enabled,
          this.separatePlatforms,
          this.platformIconPosition == "left" ? PlatformIconPosition.LEFT
              : this.platformIconPosition == "top" ? PlatformIconPosition.TOP
              : this.platformIconPosition == "right" ? PlatformIconPosition.RIGHT
              : this.platformIconPosition == "bottom" ? PlatformIconPosition.BOTTOM
              : null
      );
    }
  }
}
