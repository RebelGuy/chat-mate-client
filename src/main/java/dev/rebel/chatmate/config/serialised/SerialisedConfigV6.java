package dev.rebel.chatmate.config.serialised;

import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.HudElement;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.util.EnumHelpers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SerialisedConfigV6 extends SerialisedConfigVersions.Version {
  public final boolean soundEnabled;
  public final boolean showChatMateOptionsInPauseMenu;
  public final int chatVerticalDisplacement;
  public final String commandMessageChatVisibility;
  public final boolean showCommandMessageStatus;
  public final boolean hudEnabled;
  public final boolean showChatPlatformIcon;
  public final SerialisedSeparableHudElement statusIndicator;
  public final SerialisedSeparableHudElement viewerCount;
  public final boolean debugModeEnabled;
  public final String[] logLevels;
  public final long lastGetChatResponse;
  public final long lastGetChatMateEventsResponse;
  public final Map<String, SerialisedHudElementTransform> hudTransforms;
  public final SerialisedLoginInfo loginInfo;
  public final List<String> chatMentionFilter;

  public SerialisedConfigV6(boolean soundEnabled,
                            boolean showChatMateOptionsInPauseMenu,
                            int chatVerticalDisplacement,
                            String commandMessageChatVisibility,
                            boolean showCommandMessageStatus,
                            boolean hudEnabled,
                            boolean showChatPlatformIcon,
                            SerialisedSeparableHudElement statusIndicator,
                            SerialisedSeparableHudElement viewerCount,
                            boolean debugModeEnabled,
                            String[] logLevels,
                            long lastGetChatResponse,
                            long lastGetChatMateEventsResponse,
                            Map<String, SerialisedHudElementTransform> hudTransforms,
                            SerialisedLoginInfo loginInfo,
                            List<String> chatMentionFilter) {
    this.soundEnabled = soundEnabled;
    this.showChatMateOptionsInPauseMenu = showChatMateOptionsInPauseMenu;
    this.chatVerticalDisplacement = chatVerticalDisplacement;
    this.commandMessageChatVisibility = commandMessageChatVisibility;
    this.showCommandMessageStatus = showCommandMessageStatus;
    this.hudEnabled = hudEnabled;
    this.showChatPlatformIcon = showChatPlatformIcon;
    this.statusIndicator = statusIndicator;
    this.viewerCount = viewerCount;
    this.debugModeEnabled = debugModeEnabled;
    this.logLevels = logLevels;
    this.lastGetChatResponse = lastGetChatResponse;
    this.lastGetChatMateEventsResponse = lastGetChatMateEventsResponse;
    this.hudTransforms = hudTransforms;
    this.loginInfo = loginInfo;
    this.chatMentionFilter = chatMentionFilter;
  }

  @Override
  public int getVersion() {
    return 6;
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

    public SerialisedHudElementTransform(SerialisedConfigV5.SerialisedHudElementTransform transform) {
      this(
          transform.x,
          transform.xAnchor,
          transform.y,
          transform.yAnchor,
          transform.positionAnchor,
          transform.screenWidth,
          transform.screenHeight,
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

  public static class SerialisedLoginInfo {
    public final @Nullable String username;
    public final @Nullable String loginToken;

    public SerialisedLoginInfo(@Nullable String username, @Nullable String loginToken) {
      this.username = username;
      this.loginToken = loginToken;
    }

    public SerialisedLoginInfo(Config.LoginInfo loginInfo) {
      this(loginInfo.username, loginInfo.loginToken);
    }

    public Config.LoginInfo deserialise() {
      return new Config.LoginInfo(this.username, this.loginToken);
    }
  }
}
