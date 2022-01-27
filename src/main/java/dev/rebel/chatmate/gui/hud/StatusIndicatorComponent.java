package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.StatusService.SimpleStatus;
import net.minecraft.client.renderer.GlStateManager;

import java.util.HashMap;
import java.util.Map;

public class StatusIndicatorComponent extends Box implements IHudComponent {
  private final StatusService statusService;
  private final Map<SimpleStatus, ImageComponent> statusIndicators;

  public StatusIndicatorComponent(StatusService statusService) {
    super(5, 5, 32, 32, true, false);
    this.statusService = statusService;

    int x = 0, y = 0;
    float scale = 0.5f;
    boolean canRescale = false, canTranslate = true;

    this.statusIndicators = new HashMap<>();
    this.statusIndicators.put(SimpleStatus.OK_LIVE, new ImageComponent(Asset.STATUS_INDICATOR_GREEN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.OK_OFFLINE, new ImageComponent(Asset.STATUS_INDICATOR_CYAN, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.YOUTUBE_UNREACHABLE, new ImageComponent(Asset.STATUS_INDICATOR_ORANGE, x, y, scale, canRescale, canTranslate));
    this.statusIndicators.put(SimpleStatus.SERVER_UNREACHABLE, new ImageComponent(Asset.STATUS_INDICATOR_RED, x, y, scale, canRescale, canTranslate));
  }

  @Override
  public float getContentScale() { return 1; }

  @Override
  public boolean canRescaleContent() { return false; }

  @Override
  public void onRescaleContent(float newScale) { }

  @Override
  public void render(RenderContext context) {
    SimpleStatus status = this.statusService.getSimpleStatus();

    GlStateManager.pushMatrix();
    GlStateManager.translate(this.x, this.y, 0);

    this.statusIndicators.get(status).render(context);

    GlStateManager.popMatrix();
  }
}
