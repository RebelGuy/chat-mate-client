package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.RenderContext;
import net.minecraft.client.renderer.GlStateManager;

public class StatusIndicatorComponent extends Box implements IHudComponent {
  private final ImageComponent statusIndicator;

  public StatusIndicatorComponent() {
    super(10, 10, 32, 32, true, false);

    this.statusIndicator = new ImageComponent(Asset.STATUS_INDICATOR, 0, 0, 1, false, false);
  }

  @Override
  public float getContentScale() { return 1; }

  @Override
  public boolean canRescaleContent() { return false; }

  @Override
  public void onRescaleContent(float newScale) { }

  @Override
  public void render(RenderContext context) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(this.x, this.y, 0);

    this.statusIndicator.render(context);

    GlStateManager.popMatrix();
  }
}
