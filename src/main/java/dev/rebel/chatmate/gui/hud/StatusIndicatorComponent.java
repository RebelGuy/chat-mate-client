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

  public StatusIndicatorComponent(DimFactory dimFactory, float initialScale, StatusService statusService, Config config) {
    // giving values in Gui coords. so at 2x gui scale, with an initial scale of 0.5f, will be at screen coords (20, 20) with a screen size of 16x16
    super(
        dimFactory,
        dimFactory.fromGui(INITIAL_X_GUI),
        dimFactory.fromGui(INITIAL_Y_GUI),
        dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? BASE_SIZE_GUI * initialScale : 0),
        dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? BASE_SIZE_GUI * initialScale : 0),
        true,
        true
    );
    this.dimFactory = dimFactory;
    this.statusService = statusService;
    this.config = config;
    this.initialScale = initialScale;
    this.scale = initialScale;

    Dim x = dimFactory.zero();
    Dim y = dimFactory.zero();
    float scale = 1;
    boolean canRescale = false, canTranslate = false;

    this.statusIndicators = new HashMap<>();
    this.statusIndicators.put(SimpleStatus.OK_LIVE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_GREEN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.OK_OFFLINE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_CYAN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.YOUTUBE_UNREACHABLE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_ORANGE, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.SERVER_UNREACHABLE, new ImageComponent(dimFactory, Asset.STATUS_INDICATOR_RED, x, y, scale, canRescale, canTranslate));

    this.config.getShowStatusIndicatorEmitter().listen(this::onShowStatusIndicator);
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
    Dim newH = this.dimFactory.fromGui(BASE_SIZE_GUI * newScale);
    this.onResize(newW, newH, Anchor.MIDDLE);
    this.scale = newScale;
  }

  @Override
  public void render(RenderContext context) {
    if (!this.config.getShowStatusIndicatorEmitter().get()) {
      return;
    }

    SimpleStatus status = this.statusService.getSimpleStatus();

    GlStateManager.pushMatrix();
    GlStateManager.translate(this.getX().getScreen(), this.getY().getScreen(), 0);
    GlStateManager.scale(this.scale, this.scale, 1);

    this.statusIndicators.get(status).render(context);

    GlStateManager.popMatrix();
  }

  private void onShowStatusIndicator(boolean enabled) {
    Dim x = dimFactory.fromGui(INITIAL_X_GUI);
    Dim y = dimFactory.fromGui(INITIAL_Y_GUI);
    Dim w = dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? BASE_SIZE_GUI * this.scale : 0);
    Dim h = dimFactory.fromGui(config.getShowStatusIndicatorEmitter().get() ? BASE_SIZE_GUI * this.scale : 0);
    this.onTranslate(x, y);
    this.onResize(w, h, Anchor.MIDDLE);
    this.scale = this.initialScale;
  }
}
