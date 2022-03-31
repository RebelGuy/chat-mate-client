package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.StatusService.SimpleStatus;
import net.minecraft.client.renderer.GlStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StatusIndicatorComponent extends Box implements IHudComponent {
  private final DimFactory dimFactory;
  private final StatusService statusService;
  private final Config config;
  private final Map<SimpleStatus, ImageComponent> statusIndicators;
  private final float initialScale;
  private float scale;

  private final static int BASE_SIZE_GUI = 16;
  private final static int INITIAL_X_GUI = 10;
  private final static int INITIAL_Y_GUI = 10;
  private final static int IDENTIFIED_Y_OFFSET_GUI = 26;

  private final Consumer<Boolean> _onShowStatusIndicator = this::onShowStatusIndicator;
  private final Consumer<Boolean> _onIdentifyPlatforms = this::onIdentifyPlatforms;

  public StatusIndicatorComponent(DimFactory dimFactory, float initialScale, StatusService statusService, Config config) {
    // giving values in Gui coords. so at 2x gui scale, with an initial scale of 0.5f, will be at screen coords (20, 20) with a screen size of 16x16
    super(
        dimFactory,
        dimFactory.fromGui(INITIAL_X_GUI),
        dimFactory.fromGui(INITIAL_Y_GUI),
        dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? BASE_SIZE_GUI * initialScale : 0),
        dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? getUnscaledHeight(config.getIdentifyPlatforms().get()) * initialScale : 0),
        true,
        true
    );
    this.dimFactory = dimFactory;
    this.statusService = statusService;
    this.config = config;
    this.initialScale = initialScale;
    this.scale = initialScale;

    Dim x = dimFactory.zeroGui();
    Dim y = dimFactory.zeroGui();
    float scale = 1;
    boolean canRescale = false, canTranslate = false;

    this.statusIndicators = new HashMap<>();
    this.statusIndicators.put(SimpleStatus.OK_LIVE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_GREEN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.OK_OFFLINE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_CYAN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.PLATFORM_UNREACHABLE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_ORANGE, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.SERVER_UNREACHABLE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_RED, x, y, scale, canRescale, canTranslate));

    this.config.getShowStatusIndicatorEmitter().onChange(this._onShowStatusIndicator, this);
    this.config.getIdentifyPlatforms().onChange(this._onIdentifyPlatforms, this);
  }

  @Override
  public float getContentScale() { return this.scale; }

  @Override
  public boolean canRescaleContent() { return true; }

  @Override
  public void onRescaleContent(float newScale) {
    newScale = Math.max(0.2f, newScale);
    newScale = Math.min(newScale, 5.0f);
    if (this.scale == newScale) {
      return;
    }

    Dim newW = this.dimFactory.fromGui(BASE_SIZE_GUI * newScale);
    Dim newH = this.dimFactory.fromGui(getUnscaledHeight(config.getIdentifyPlatforms().get()) * newScale);
    this.onResize(newW, newH, Anchor.MIDDLE);
    this.scale = newScale;
  }

  @Override
  public void render(RenderContext context) {
    if (!this.config.getShowStatusIndicatorEmitter().get()) {
      return;
    }

    if (this.config.getIdentifyPlatforms().get()) {
      this.renderStatus(context, this.statusService.getYoutubeSimpleStatus(), 0);
      this.renderStatus(context, this.statusService.getTwitchSimpleStatus(), IDENTIFIED_Y_OFFSET_GUI * this.scale);
    } else {
      this.renderStatus(context, this.statusService.getAggregateSimpleStatus(), 0);
    }
  }

  private void renderStatus(RenderContext context, SimpleStatus status, float yOffset) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(this.getX().getGui(), this.getY().getGui() + yOffset, 0);
    GlStateManager.scale(this.scale, this.scale, 1);

    this.statusIndicators.get(status).render(context);

    GlStateManager.popMatrix();
  }

  private void onShowStatusIndicator(boolean enabled) {
    Dim x = dimFactory.fromGui(INITIAL_X_GUI);
    Dim y = dimFactory.fromGui(INITIAL_Y_GUI);
    Dim w = dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? BASE_SIZE_GUI * this.scale : 0);
    Dim h = dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? getUnscaledHeight(config.getIdentifyPlatforms().get()) * this.scale : 0);
    this.setRect(x, y, w, h);
    this.scale = this.initialScale;
  }

  private void onIdentifyPlatforms(boolean identify) {
    Dim height = this.dimFactory.fromGui(getUnscaledHeight(config.getIdentifyPlatforms().get()) * this.scale);
    this.setRect(this.getX(), this.getY(), this.getWidth(), height);
  }

  private static float getUnscaledHeight(boolean identifiedPlatforms) {
    return identifiedPlatforms ? BASE_SIZE_GUI + IDENTIFIED_Y_OFFSET_GUI : BASE_SIZE_GUI;
  }
}
