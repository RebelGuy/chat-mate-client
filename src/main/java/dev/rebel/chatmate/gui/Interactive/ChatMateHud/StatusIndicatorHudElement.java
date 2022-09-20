package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.StateManagement.AnimatedEvent;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.hud.IHudComponent.Anchor;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.StatusService.SimpleStatus;
import dev.rebel.chatmate.services.events.ServerLogEventService;
import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;
import dev.rebel.chatmate.services.util.Collections;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_ORANGE;
import static dev.rebel.chatmate.Asset.STATUS_INDICATOR_RED;
import static dev.rebel.chatmate.gui.Interactive.ChatMateHud.StatusIndicatorHudElement.*;

public class StatusIndicatorHudElement extends SimpleHudElementWrapper<BlockElement> {
  private final Config config;
  private final BlockElement indicatorContainer;

  private final static int INITIAL_X_GUI = 10;
  private final static int INITIAL_Y_GUI = 10;

  private final static long SERVER_LOG_ANIMATION_DURATION = 1000;
  private final static float SERVER_LOG_ANIMATION_MAX_SCALE = 3;
  private final IndicatorElement mainIndicator;
  private final IndicatorElement secondaryIndicator;

  public StatusIndicatorHudElement(InteractiveContext context, IElement parent, StatusService statusService, Config config, ServerLogEventService serverLogEventService) {
    // giving values in Gui coords. so at 2x gui scale, with an initial scale of 0.5f, will be at screen coords (20, 20) with a screen size of 16x16
    super(context, parent);
    super.setCanDrag(true);
    super.setCanScale(true);
    super.setDefaultPosition(new DimPoint(gui(INITIAL_X_GUI), gui(INITIAL_Y_GUI)), Anchor.TOP_LEFT);
    super.setResizeAnchor(Anchor.MIDDLE);
    super.setHudElementFilter(); // shown everywhere

    this.mainIndicator = new IndicatorElement(context, this, true, statusService, config, serverLogEventService)
        .cast();
    this.secondaryIndicator = new IndicatorElement(context, this, false, statusService, config, serverLogEventService)
        .setMargin(new RectExtension(ZERO, ZERO, gui(10), ZERO))
        .cast();
    this.indicatorContainer = new BlockElement(context, this)
        .addElement(this.mainIndicator)
        .addElement(this.secondaryIndicator)
        .cast();
    super.setElement(this.indicatorContainer);

    this.config = config;
  }

  @Override
  protected void onRescaleContent(DimRect oldBox, float oldScale, float newScale) {
    this.mainIndicator.setScale(newScale);
    this.secondaryIndicator.setScale(newScale);
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

    public IndicatorElement(InteractiveContext context, IElement parent, boolean isMainIndicator, StatusService statusService, Config config, ServerLogEventService serverLogEventService) {
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

      config.getIdentifyPlatforms().onChange(this::updateVisibility);
      this.updateVisibility(config.getIdentifyPlatforms().get());

      this.serverLogEvents = new AnimatedEvent<>(SERVER_LOG_ANIMATION_DURATION);

      this.serverLogEventService.onWarning(this._onServerLogWarning, this);
      this.serverLogEventService.onError(this._onServerLogError, this);
      this.defaultSize = context.dimFactory.fromGui(8); // size at 100% scale
      this.setScale(1);
    }

    private void updateVisibility(boolean identifyPlatforms) {
      if (!this.isMainIndicator) {
        super.setVisible(identifyPlatforms);
      }
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
      if (this.config.getIdentifyPlatforms().get()) {
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

      if (this.config.getShowServerLogsHeartbeat().get()) {
        for (Tuple<Texture, Float> logEvent : this.serverLogEvents.getAllFracs()) {
          Texture logTexture = logEvent.getFirst();
          DimPoint centre = new DimPoint(super.context.dimFactory.fromGui(logTexture.width).over(2), super.context.dimFactory.fromGui(logTexture.height).over(2));
          float scale = logEvent.getSecond() * SERVER_LOG_ANIMATION_MAX_SCALE;
          float alpha = 1 - logEvent.getSecond();
          RendererHelpers.drawTextureCentred(super.context.minecraft.getTextureManager(), super.context.dimFactory, logTexture, centre, scale, Colour.WHITE.withAlpha(alpha));
        }
      }

      super.renderElement();
    }
  }
}
