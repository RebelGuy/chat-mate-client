package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.StateManagement.AnimatedEvent;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.StatusService.SimpleStatus;
import dev.rebel.chatmate.services.events.ServerLogEventService;
import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;
import dev.rebel.chatmate.services.util.EnumHelpers;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_ORANGE;
import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_RED;

public class StatusIndicatorHudElement extends SimpleHudElementWrapper<ContainerElement> {
  private final static int INITIAL_X_GUI = 10;
  private final static int INITIAL_Y_GUI = 10;

  private final static long SERVER_LOG_ANIMATION_DURATION = 1000;
  private final static float SERVER_LOG_ANIMATION_MAX_SCALE = 3;

  private final Config config;

  private final Dim defaultIconSize;
  private final boolean isMainIndicator;
  private final IndicatorElement indicator;
  private final float platformIconScale; // image scale at 100% hud scale
  private final ImageElement platformIcon;

  private @Nullable PlatformIconPosition prevIconPosition; // null if it isn't shown
  private boolean requiresSecondPass; // if true, should re-initialise the container after the box has been set

  public StatusIndicatorHudElement(InteractiveContext context, IElement parent, @Nullable HudElement parentIndicatorElement, StatusService statusService, Config config, ServerLogEventService serverLogEventService) {
    super(context, parent);
    super.setCanDrag(true);
    super.setCanScale(true);
    super.setDefaultPosition(parentIndicatorElement == null ? new DimPoint(gui(INITIAL_X_GUI), gui(INITIAL_Y_GUI)) : new DimPoint(gui(INITIAL_Y_GUI), gui(INITIAL_Y_GUI + 13)), Anchor.TOP_LEFT);
    super.setScrollResizeAnchor(Anchor.MIDDLE);
    super.setContentResizeAnchor(Anchor.TOP_LEFT); // when toggling the platform icon, fix the hud element's position. this can result in overlap between the indicators and icons if set to top/bottom, and it is up to the user to fix this
    super.setHudElementFilter(); // shown everywhere
    this.config = config;
    this.defaultIconSize = gui(8); // size at 100% scale
    this.isMainIndicator = parentIndicatorElement == null;

    this.prevIconPosition = null;
    this.requiresSecondPass = false;

    this.indicator = new IndicatorElement(context, this, this.isMainIndicator, this.defaultIconSize, statusService, config, serverLogEventService)
        .setMargin(new RectExtension(gui(2)))
        .cast();

    Texture image = this.isMainIndicator ? Asset.LOGO_YOUTUBE : Asset.LOGO_TWITCH;
    this.platformIconScale = this.defaultIconSize.over(gui(image.width));
    this.platformIcon = new ImageElement(context, this)
        .setImage(image)
        .setScale(this.platformIconScale)
        .setMargin(new RectExtension(gui(2)))
        .cast();

    config.getStatusIndicatorEmitter().onChange(this::onChangeStatusIndicatorSettings);
    this.onChangeStatusIndicatorSettings(config.getStatusIndicatorEmitter().get());

    config.getStatusIndicatorEmitter().onChange(this::onChangeStatusIndicatorConfig);
    this.onChangeStatusIndicatorConfig(config.getStatusIndicatorEmitter().get());
  }

  private void onChangeStatusIndicatorSettings(Config.SeparableHudElement settings) {
    ContainerElement container;

    // updating (or toggling) the icon position is done in two steps so that the indicator appears stationary, while the icon "orbits" around it
    // 1. remove the existing icon by creating a new container that is resize-anchored such that the indicator doesn't move (this is done here)
    // 2. add the new icon (if applicable) to the container, changing the resize anchor again such that the indicator doesn't move (this is done just after setting the box)

    Anchor resizeAnchor = Anchor.TOP_LEFT;
    if (this.prevIconPosition != null) {
      switch (this.prevIconPosition) {
        case LEFT:
          resizeAnchor = Anchor.RIGHT_CENTRE;
          break;
        case RIGHT:
          resizeAnchor = Anchor.LEFT_CENTRE;
          break;
        case TOP:
          resizeAnchor = Anchor.BOTTOM_CENTRE;
          break;
        case BOTTOM:
          resizeAnchor = Anchor.TOP_CENTRE;
          break;
        default:
          throw EnumHelpers.<PlatformIconPosition>assertUnreachable(settings.platformIconPosition);
      }
    }

    super.setContentResizeAnchor(resizeAnchor);
    super.setElement(new BlockElement(super.context, this).addElement(this.indicator)); // first pass

    if (settings.separatePlatforms && settings.showPlatformIcon) {
      this.prevIconPosition = settings.platformIconPosition;
      this.requiresSecondPass = true;
    } else {
      this.prevIconPosition = null;
      this.requiresSecondPass = false;
    }
  }

  private void onChangeStatusIndicatorConfig(Config.SeparableHudElement data) {
    this.updateVisibility(data.enabled, data.separatePlatforms);
  }

  private void updateVisibility(boolean indicatorEnabled, boolean separatePlatforms) {
    if (this.isMainIndicator) {
      super.setVisible(indicatorEnabled);
    } else {
      super.setVisible(indicatorEnabled && separatePlatforms);
    }
  }

  @Override
  protected void onRescaleContent(DimRect oldBox, float oldScale, float newScale) {
    this.indicator.setScale(newScale);
    this.platformIcon.setScale(this.platformIconScale * newScale);
  }

  @Override
  public void onHudBoxSet(DimRect box) {
    super.onHudBoxSet(box);

    if (this.requiresSecondPass) {
      this.requiresSecondPass = false;

      Config.SeparableHudElement settings = this.config.getStatusIndicatorEmitter().get();
      if (!(settings.separatePlatforms && settings.showPlatformIcon)) {
        return;
      }

      // the current box is only for the indicator. here we add the icon as well (in a new container, so the ordering/layout is right)
      ContainerElement container;
      Anchor resizeAnchor;
      switch (settings.platformIconPosition) {
        case TOP:
          container = new BlockElement(super.context, this)
              .addElement(this.platformIcon)
              .addElement(this.indicator);
          resizeAnchor = Anchor.BOTTOM_CENTRE;
          break;
        case BOTTOM:
          container = new BlockElement(super.context, this)
              .addElement(this.indicator)
              .addElement(this.platformIcon);
          resizeAnchor = Anchor.TOP_CENTRE;
          break;
        case LEFT:
          container = new InlineElement(super.context, this)
              .addElement(this.platformIcon)
              .addElement(this.indicator);
          resizeAnchor = Anchor.RIGHT_CENTRE;
          break;
        case RIGHT:
          container = new InlineElement(super.context, this)
              .addElement(this.indicator)
              .addElement(this.platformIcon);
          resizeAnchor = Anchor.LEFT_CENTRE;
          break;
        default:
          throw EnumHelpers.<PlatformIconPosition>assertUnreachable(settings.platformIconPosition);
      }

      super.setContentResizeAnchor(resizeAnchor);
      super.setElement(container);
    }
  }

  protected static class IndicatorElement extends ImageElement {
    private final Map<SimpleStatus, Texture> statusTextures;
    private final boolean isMainIndicator;
    private final StatusService statusService;
    private final Config config;
    private final ServerLogEventService serverLogEventService;
    private final AnimatedEvent<Texture> serverLogEvents;

    private final Function<EventIn, EventOut> _onServerLogError = this::onServerLogError;
    private final Function<EventIn, EventOut> _onServerLogWarning = this::onServerLogWarning;
    private final Dim defaultSize;

    public IndicatorElement(InteractiveContext context, IElement parent, boolean isMainIndicator, Dim defaultSize, StatusService statusService, Config config, ServerLogEventService serverLogEventService) {
      super(context, parent);
      this.isMainIndicator = isMainIndicator;
      this.statusService = statusService;
      this.config = config;
      this.serverLogEventService = serverLogEventService;

      this.statusTextures = new HashMap<>();
      this.statusTextures.put(SimpleStatus.OK_LIVE, Asset.STATUS_INDICATOR_GREEN);
      this.statusTextures.put(SimpleStatus.OK_OFFLINE, Asset.STATUS_INDICATOR_CYAN);
      this.statusTextures.put(SimpleStatus.OK_NO_LIVESTREAM, Asset.STATUS_INDICATOR_BLUE);
      this.statusTextures.put(SimpleStatus.PLATFORM_UNREACHABLE, Asset.STATUS_INDICATOR_ORANGE);
      this.statusTextures.put(SimpleStatus.SERVER_UNREACHABLE, Asset.STATUS_INDICATOR_RED);

      super.setImage(Asset.STATUS_INDICATOR_RED);

      this.serverLogEvents = new AnimatedEvent<>(SERVER_LOG_ANIMATION_DURATION);

      this.serverLogEventService.onWarning(this._onServerLogWarning, this);
      this.serverLogEventService.onError(this._onServerLogError, this);
      this.defaultSize = defaultSize;
      this.setScale(1);
    }

    private EventOut onServerLogError(EventIn eventIn) {
      this.serverLogEvents.onEvent(STATUS_INDICATOR_RED);
      return null;
    }

    private EventOut onServerLogWarning(EventIn eventIn) {
      this.serverLogEvents.onEvent(STATUS_INDICATOR_ORANGE);
      return null;
    }

    @Override
    public ImageElement setScale(float scale) {
      Dim actualSize = gui(STATUS_INDICATOR_RED.width);
      return super.setScale(this.defaultSize.over(actualSize) * scale);
    }

    @Override
    protected void renderElement() {
      SimpleStatus status;
      if (this.config.getStatusIndicatorEmitter().get().separatePlatforms) {
        if (this.isMainIndicator) {
          status = this.statusService.getYoutubeSimpleStatus();
        } else {
          status = this.statusService.getTwitchSimpleStatus();
        }
      } else {
        status = this.statusService.getAggregateSimpleStatus();
      }

      Texture texture = this.statusTextures.get(status);
      super.setImage(texture);

      if (this.config.getShowServerLogsHeartbeat().get() && this.isMainIndicator) {
        for (Tuple<Texture, Float> logEvent : this.serverLogEvents.getAllFracs()) {
          Texture logTexture = logEvent.getFirst();
          float scale = logEvent.getSecond() * SERVER_LOG_ANIMATION_MAX_SCALE * super.scale;
          float alpha = 1 - logEvent.getSecond();
          DimPoint pos =  this.getContentBox().getCentre();
          RendererHelpers.drawTextureCentred(super.context.minecraft.getTextureManager(), super.context.dimFactory, logTexture, pos, scale, Colour.WHITE.withAlpha(alpha));
        }
      }

      super.renderElement();
    }
  }
}
