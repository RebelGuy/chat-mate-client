package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.StatusService.SimpleStatus;
import net.minecraft.client.renderer.GlStateManager;

import java.util.HashMap;
import java.util.Map;

public class StatusIndicatorComponent extends Box implements IHudComponent {
  private final StatusService statusService;
  private final Config config;
  private final Map<SimpleStatus, ImageComponent> statusIndicators;
  private final float initialScale;
  private float scale;

  private final static int baseGuiSize = 16;

  public StatusIndicatorComponent(int guiScaleMultiplier, float initialScale, StatusService statusService, Config config) {
    // giving values in Gui coords. so at 2x gui scale, with an initial scale of 0.5f, will be at screen coords (20, 20) with a screen size of 16x16
    super(
        guiScaleMultiplier,
        10,
        10,
        config.getShowStatusIndicator().get() ? baseGuiSize * initialScale : 0,
        config.getShowStatusIndicator().get() ? baseGuiSize * initialScale : 0,
        true,
        true
    );
    this.statusService = statusService;
    this.config = config;
    this.initialScale = initialScale;
    this.scale = initialScale;

    float x = 0, y = 0, scale = 1;
    boolean canRescale = false, canTranslate = false;

    this.statusIndicators = new HashMap<>();
    this.statusIndicators.put(SimpleStatus.OK_LIVE, new ImageComponent(Asset.STATUS_INDICATOR_GREEN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.OK_OFFLINE, new ImageComponent(Asset.STATUS_INDICATOR_CYAN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.YOUTUBE_UNREACHABLE, new ImageComponent(Asset.STATUS_INDICATOR_ORANGE, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.SERVER_UNREACHABLE, new ImageComponent(Asset.STATUS_INDICATOR_RED, x, y, scale, canRescale, canTranslate));

    this.config.getShowStatusIndicator().listen(this::onShowStatusIndicator);
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

    this.onResize(baseGuiSize * newScale, baseGuiSize * newScale, Anchor.MIDDLE);
    this.scale = newScale;
  }

  @Override
  public void render(RenderContext context) {
    if (!this.config.getShowStatusIndicator().get()) {
      return;
    }

    SimpleStatus status = this.statusService.getSimpleStatus();

    GlStateManager.pushMatrix();
    GlStateManager.translate(this.getX(), this.getY(), 0);
    GlStateManager.scale(this.scale, this.scale, 1);

    this.statusIndicators.get(status).render(context);

    GlStateManager.popMatrix();
  }

  private void onShowStatusIndicator(boolean enabled) {
    float x = 10;
    float y = 10;
    float w = enabled ? baseGuiSize * this.scale : 0;
    float h = enabled ? baseGuiSize * this.scale : 0;
    this.onTranslate(x, y);
    this.onResize(w, h, Anchor.MIDDLE);
    this.scale = this.initialScale;
  }
}
