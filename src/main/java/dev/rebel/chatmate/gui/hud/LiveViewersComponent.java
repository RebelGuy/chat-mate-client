package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class LiveViewersComponent extends Box implements IHudComponent {
  private final static int COLOR = 0xFFFFFFFF;
  private final static int MAX_REEL_VALUE = 99;
  private final static int INITIAL_X_GUI = 23;
  private final static int INITIAL_Y_GUI = 10;
  private final static int IDENTIFIED_Y_OFFSET_GUI = 13;

  private final DimFactory dimFactory;
  private final Config config;
  private final float initialScale;
  private final StatusService statusService;
  private final Minecraft minecraft;
  private float scale;
  private DigitReel reel1;
  private DigitReel reel2;

  private final Consumer<Boolean> _onShowLiveViewers = this::onShowLiveViewers;
  private final Consumer<Boolean> _onIdentifyPlatforms = this::onIdentifyPlatforms;

  public LiveViewersComponent(DimFactory dimFactory, float initialScale, StatusService statusService, Config config, Minecraft minecraft) {
    super(dimFactory, dimFactory.fromGui(INITIAL_X_GUI), dimFactory.fromGui(INITIAL_Y_GUI), dimFactory.zeroGui(), dimFactory.zeroGui(), true, true);
    this.dimFactory = dimFactory;
    this.config = config;
    this.initialScale = initialScale;
    this.scale = initialScale;
    this.statusService = statusService;
    this.minecraft = minecraft;

    this.reel1 = new DigitReel(minecraft, dimFactory);
    this.reel2 = new DigitReel(minecraft, dimFactory);

    this.config.getShowLiveViewersEmitter().onChange(this._onShowLiveViewers, this);
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

    this.onResize(this.getTextWidth().times(newScale), this.getUnscaledHeight().times(newScale), this.getAnchor());
    this.scale = newScale;
  }

  private Anchor getAnchor() {
    // resizing in the centre feels weird when identityPlatforms is enabled
    return this.config.getIdentifyPlatforms().get() ? Anchor.TOP_LEFT : Anchor.LEFT_CENTRE;
  }

  @Override
  public void render(RenderContext context) {
    if (!this.config.getShowLiveViewersEmitter().get() || this.getFontRenderer() == null) {
      return;
    }

    // we have to update this constantly because of dynamic content
    // this.onResize(this.getTextWidth().times(this.scale), this.getUnscaledHeight().times(this.scale), this.getAnchor());

    if (this.config.getIdentifyPlatforms().get()) {
      this.renderViewerCount(this.reel1, this.dimFactory.zeroGui(), this.statusService.getYoutubeLiveViewerCount());
      this.renderViewerCount(this.reel2, this.dimFactory.fromGui(IDENTIFIED_Y_OFFSET_GUI * this.scale), this.statusService.getTwitchLiveViewerCount());
    } else {
      this.renderViewerCount(this.reel1, this.dimFactory.zeroGui(), this.statusService.getTotalLiveViewerCount());
    }
  }

  private void renderViewerCount(DigitReel reel, Dim yOffset, @Nullable Integer viewCount) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(this.getX().getGui(), this.getY().plus(yOffset).getGui(), 0);
    GlStateManager.scale(this.scale, this.scale, 1);

    String text = this.getText(viewCount);
    if (viewCount == null || viewCount > MAX_REEL_VALUE) {
      this.getFontRenderer().drawStringWithShadow(text, 0, 0, COLOR);
    } else {
      Dim digitWidth = this.dimFactory.fromGui(this.getFontRenderer().getCharWidth('0'));
      Dim digitPadding = this.dimFactory.fromGui(1);
      Dim digitHeight = this.getTextHeight();
      reel.drawReel(text, digitWidth, digitPadding, digitHeight);
    }

    GlStateManager.popMatrix();
  }

  private void onShowLiveViewers(boolean enabled) {
    this.scale = this.initialScale;

    Dim x = this.dimFactory.fromGui(INITIAL_X_GUI);
    Dim y = this.dimFactory.fromGui(INITIAL_Y_GUI);
    Dim width = this.getTextWidth().times(this.scale);
    Dim height = this.getUnscaledHeight().times(this.scale);
    this.setRect(x, y, width, height);

    this.reel1 = new DigitReel(minecraft, dimFactory);
    this.reel2 = new DigitReel(minecraft, dimFactory);
  }

  private void onIdentifyPlatforms(boolean identifyPlatforms) {
    Dim width = this.getTextWidth().times(this.scale);
    Dim height = this.getUnscaledHeight().times(this.scale);
    if (identifyPlatforms) {
      // the anchor changes in such a way that we must also update the position.
      this.setRect(x, y, width, height);
    } else {
      this.setRect(x, y, width, height);
    }
  }

  private Dim getTextWidth() {
    if (this.getFontRenderer() == null) {
      return this.dimFactory.zeroGui();
    } else {
      String text1, text2;
      if (this.config.getIdentifyPlatforms().get()) {
        text1 = this.getText(this.statusService.getYoutubeLiveViewerCount());
        text2 = this.getText(this.statusService.getTwitchLiveViewerCount());
      } else {
        text1 = this.getText(this.statusService.getTotalLiveViewerCount());
        text2 = "";
      }
      return this.dimFactory.fromGui(Math.max(this.getFontRenderer().getStringWidth(text1), this.getFontRenderer().getStringWidth(text2)));
    }
  }

  private Dim getTextHeight() {
    if (this.getFontRenderer() == null) {
      return this.dimFactory.zeroGui();
    } else {
      return this.dimFactory.fromGui(this.getFontRenderer().FONT_HEIGHT);
    }
  }

  private String getText(@Nullable Integer viewCount) {
    if (viewCount == null) {
      return "n/a";
    } else {
      return String.format("%02d", viewCount);
    }
  }

  private @Nullable FontRenderer getFontRenderer() {
    return this.minecraft.fontRendererObj;
  }

  private Dim getUnscaledHeight() {
    Dim textHeight = this.getTextHeight();
    if (this.config.getIdentifyPlatforms().get()) {
      return textHeight.plus(this.dimFactory.fromGui(IDENTIFIED_Y_OFFSET_GUI));
    } else {
      return textHeight;
    }
  }
}
