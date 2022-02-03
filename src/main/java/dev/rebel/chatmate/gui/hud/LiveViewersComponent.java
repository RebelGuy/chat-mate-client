package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.Asset;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.StatusService.SimpleStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LiveViewersComponent extends Box implements IHudComponent {
  private final StatusService statusService;
  private final Config config;
  private final float initialScale;
  private float scale;
  private final Minecraft minecraft;

  public LiveViewersComponent(int guiScaleMultiplier, float initialScale, StatusService statusService, Config config, Minecraft minecraft) {
    // giving values in Gui coords. so at 2x gui scale, with an initial scale of 0.5f, will be at screen coords (20, 20) with a screen size of 16x16
    super(guiScaleMultiplier, 23, 14, 0, 0, true, true);
    this.statusService = statusService;
    this.config = config;
    this.initialScale = initialScale;
    this.scale = initialScale;
    this.minecraft = minecraft;

    this.config.getShowLiveViewers().listen(this::onShowLiveViewers);
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

    this.onResize(this.getTextWidth() * newScale, this.getTextHeight() * newScale, Anchor.LEFT_CENTRE);
    this.scale = newScale;
  }

  @Override
  public void render(RenderContext context) {
    if (!this.config.getShowLiveViewers().get() || this.getFontRenderer() == null) {
      return;
    }

    // we have to update this constantly because of dynamic content
    this.onResize(this.getTextWidth() * this.scale, this.getTextHeight() * this.scale, Anchor.LEFT_CENTRE);

    GlStateManager.pushMatrix();
    GlStateManager.translate(this.getX(), this.getY(), 0);
    GlStateManager.scale(this.scale, this.scale, 1);

    int color = 0xFFFFFFFF;
    String text = this.getText();
    this.getFontRenderer().drawStringWithShadow(text, 0, 0, color);

    GlStateManager.popMatrix();
  }

  private void onShowLiveViewers(boolean enabled) {
    float x = 10;
    float y = 10;
    float w = enabled ? this.getTextWidth() * this.scale : 0;
    float h = enabled ? this.getTextHeight() * this.scale : 0;
    this.setRect(x, y, w, h);
    this.scale = this.initialScale;
  }

  private float getTextWidth() {
    if (this.getFontRenderer() == null) {
      return 0;
    } else {
      String text = this.getText();
      return this.getFontRenderer().getStringWidth(text);
    }
  }

  private float getTextHeight() {
    if (this.getFontRenderer() == null) {
      return 0;
    } else {
      return this.getFontRenderer().FONT_HEIGHT;
    }
  }
  
  private String getText() {
    @Nullable Integer count = this.statusService.getLiveViewerCount();
    if (count == null) {
      return "n/a";
    } else {
      return count.toString();
    }
  }

  private @Nullable FontRenderer getFontRenderer() {
    return this.minecraft.fontRendererObj;
  }
}
