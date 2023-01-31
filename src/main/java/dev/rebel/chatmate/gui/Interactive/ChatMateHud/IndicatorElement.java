package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.ImageElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.StateManagement.AnimatedEvent;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.events.models.EventData;
import net.minecraft.util.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_ORANGE;
import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_RED;

public class IndicatorElement extends ImageElement implements SeparableHudElement.ISeparableElement {
  private final static long SERVER_LOG_ANIMATION_DURATION = 1000;
  private final static float SERVER_LOG_ANIMATION_MAX_SCALE = 3;

  private final Map<StatusService.SimpleStatus, Asset.Texture> statusTextures;
  private final boolean isMainIndicator;
  private final StatusService statusService;
  private final Config config;
  private final AnimatedEvent<Asset.Texture> serverLogEvents;

  private final Function<EventData.EventIn, EventData.EventOut> _onServerLogError = this::onServerLogError;
  private final Function<EventData.EventIn, EventData.EventOut> _onServerLogWarning = this::onServerLogWarning;
  private final Dim defaultHeight;

  public IndicatorElement(InteractiveScreen.InteractiveContext context, IElement parent, boolean isMainIndicator, Dim defaultHeight, StatusService statusService, Config config) {
    super(context, parent);
    this.isMainIndicator = isMainIndicator;
    this.statusService = statusService;
    this.config = config;

    this.statusTextures = new HashMap<>();
    this.statusTextures.put(StatusService.SimpleStatus.OK_LIVE, Asset.STATUS_INDICATOR_GREEN);
    this.statusTextures.put(StatusService.SimpleStatus.OK_OFFLINE, Asset.STATUS_INDICATOR_CYAN);
    this.statusTextures.put(StatusService.SimpleStatus.OK_NO_LIVESTREAM, Asset.STATUS_INDICATOR_BLUE);
    this.statusTextures.put(StatusService.SimpleStatus.PLATFORM_UNREACHABLE, Asset.STATUS_INDICATOR_ORANGE);
    this.statusTextures.put(StatusService.SimpleStatus.SERVER_UNREACHABLE, Asset.STATUS_INDICATOR_RED);

    super.setImage(Asset.STATUS_INDICATOR_RED);

    this.serverLogEvents = new AnimatedEvent<>(SERVER_LOG_ANIMATION_DURATION);

    this.defaultHeight = defaultHeight;
    this.setHudScale(1);
  }

  private EventData.EventOut onServerLogError(EventData.EventIn eventIn) {
    this.serverLogEvents.onEvent(STATUS_INDICATOR_RED);
    return null;
  }

  private EventData.EventOut onServerLogWarning(EventData.EventIn eventIn) {
    this.serverLogEvents.onEvent(STATUS_INDICATOR_ORANGE);
    return null;
  }

  @Override
  public void setHudScale(float scale) {
    Dim actualSize = gui(STATUS_INDICATOR_RED.height);
    super.setScale(this.defaultHeight.over(actualSize) * scale);
  }

  @Override
  protected void renderElement() {
    StatusService.SimpleStatus status;
    if (this.config.getStatusIndicatorEmitter().get().separatePlatforms) {
      if (this.isMainIndicator) {
        status = this.statusService.getYoutubeSimpleStatus();
      } else {
        status = this.statusService.getTwitchSimpleStatus();
      }
    } else {
      status = this.statusService.getAggregateSimpleStatus();
    }

    Asset.Texture texture = this.statusTextures.get(status);
    super.setImage(texture);
    super.renderElement();
  }

  public static class Factory implements SeparableHudElement.ISeparableElementFactory {
    private final Config config;
    private final StatusService statusService;

    public Factory(Config config, StatusService statusService) {
      this.config = config;
      this.statusService = statusService;
    }

    @Override
    public IndicatorElement create(InteractiveScreen.InteractiveContext context, IElement parent, boolean isMainIndicator, Dim defaultHeight) {
      return new IndicatorElement(context, parent, isMainIndicator, defaultHeight, this.statusService, this.config);
    }
  }
}
