package dev.rebel.chatmate.config.serialised;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimFactory;

import java.util.Map;
import java.util.Objects;

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
  public final Map<String, SerialisedHudElementTransform> hudTransforms;

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
                            long lastGetChatMateEventsResponse,
                            Map<String, SerialisedHudElementTransform> hudTransforms) {
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
    this.hudTransforms = hudTransforms;
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
          Objects.equals(this.platformIconPosition, "left") ? PlatformIconPosition.LEFT
              : Objects.equals(this.platformIconPosition, "top") ? PlatformIconPosition.TOP
              : Objects.equals(this.platformIconPosition, "right") ? PlatformIconPosition.RIGHT
              : Objects.equals(this.platformIconPosition, "bottom") ? PlatformIconPosition.BOTTOM
              : PlatformIconPosition.LEFT
      );
    }
  }

  public static class SerialisedHudElementTransform {
    public final float x;
    public final String xAnchor;
    public final float y;
    public final String yAnchor;
    public final float scale;

    public SerialisedHudElementTransform(float x, String xAnchor, float y, String yAnchor, float scale) {
      this.x = x;
      this.xAnchor = xAnchor;
      this.y = y;
      this.yAnchor = yAnchor;
      this.scale = scale;
    }
  }
}
