package dev.rebel.chatmate.config.serialised;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudElement;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.EnumHelpers;

import java.util.Map;
import java.util.Objects;

public class SerialisedConfigV5 extends SerialisedConfigVersions.Version {
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

  public SerialisedConfigV5(boolean soundEnabled,
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
    return 5;
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
    public final String positionAnchor;
    public final int screenWidth;
    public final int screenHeight;
    public final int screenScaleFactor;
    public final float scale;

    public SerialisedHudElementTransform(float x, String xAnchor, float y, String yAnchor, String positionAnchor, int screenWidth, int screenHeight, int screenScaleFactor, float scale) {
      this.x = x;
      this.xAnchor = xAnchor;
      this.y = y;
      this.yAnchor = yAnchor;
      this.positionAnchor = positionAnchor;
      this.screenWidth = screenWidth;
      this.screenHeight = screenHeight;
      this.screenScaleFactor = screenScaleFactor;
      this.scale = scale;
    }

    public SerialisedHudElementTransform(Config.HudElementTransform transform) {
      this(
          transform.x.getUnderlyingValue(),
          transform.x.anchor == DimAnchor.GUI ? "GUI" : "SCREEN",
          transform.y.getUnderlyingValue(),
          transform.y.anchor == DimAnchor.GUI ? "GUI" : "SCREEN",
          transform.positionAnchor.name(),
          (int)transform.screenRect.getWidth().getScreen(),
          (int)transform.screenRect.getHeight().getScreen(),
          transform.screenScaleFactor,
          transform.scale
      );
    }

    public Config.HudElementTransform deserialise(DimFactory dimFactory) {
      return new Config.HudElementTransform(
          dimFactory.fromValue(this.x, Objects.equals(this.xAnchor, "GUI") ? DimAnchor.GUI : DimAnchor.SCREEN),
          dimFactory.fromValue(this.y, Objects.equals(this.yAnchor, "GUI") ? DimAnchor.GUI : DimAnchor.SCREEN),
          EnumHelpers.fromStringOrDefault(HudElement.Anchor.class, this.positionAnchor, HudElement.Anchor.TOP_LEFT),
          new DimRect(new DimPoint(dimFactory.fromScreen(0), dimFactory.fromScreen(0)), new DimPoint(dimFactory.fromScreen(this.screenWidth), dimFactory.fromScreen(this.screenHeight))),
          this.screenScaleFactor,
          this.scale
      );
    }
  }
}
