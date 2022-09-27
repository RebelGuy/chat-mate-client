package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.events.models.ConfigEventData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class LiveViewersComponent extends Box implements IHudComponent {
  private final static int MAX_REEL_VALUE = 99;
  private final static int INITIAL_X_GUI = 23;
  private final static int INITIAL_Y_GUI = 10;
  private final static int IDENTIFIED_Y_OFFSET_GUI = 13;

  private final DimFactory dimFactory;
  private final Config config;
  private final float initialScale;
  private final StatusService statusService;
  private final Minecraft minecraft;
  private final FontEngine fontEngine;

  private final Font indicatorFont;

  private boolean initialised;
  private float scale;
  private DigitReel reel1;
  private DigitReel reel2;

  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChangeShowLiveViewers = this::onChangeShowLiveViewers;
  private final Function<ConfigEventData.In<Boolean>, ConfigEventData.Out<Boolean>> _onChangeIdentifyPlatforms = this::onChangeIdentifyPlatforms;

  public LiveViewersComponent(DimFactory dimFactory, float initialScale, StatusService statusService, Config config, Minecraft minecraft, FontEngine fontEngine) {
    super(dimFactory, dimFactory.fromGui(INITIAL_X_GUI), dimFactory.fromGui(INITIAL_Y_GUI), dimFactory.zeroGui(), dimFactory.zeroGui(), true, true);
    this.dimFactory = dimFactory;
    this.config = config;
    this.initialScale = initialScale;
    this.scale = initialScale;
    this.statusService = statusService;
    this.minecraft = minecraft;
    this.fontEngine = fontEngine;

    this.indicatorFont = new Font().withShadow(new Shadow(dimFactory));

    this.initialised = false;
    this.reel1 = new DigitReel(minecraft, dimFactory, fontEngine);
    this.reel2 = new DigitReel(minecraft, dimFactory, fontEngine);

//    this.config.getShowLiveViewersEmitter().onChange(this._onChangeShowLiveViewers, this);
//    this.config.getSeparatePlatforms().onChange(this._onChangeIdentifyPlatforms, this);
  }

  private ConfigEventData.Out<Boolean> onChangeShowLiveViewers(ConfigEventData.In<Boolean> in) {
    this.onShowLiveViewers(in.data);
    return new ConfigEventData.Out<>();
  }

  private ConfigEventData.Out<Boolean> onChangeIdentifyPlatforms(ConfigEventData.In<Boolean> in) {
    this.onIdentifyPlatforms(in.data);
    return new ConfigEventData.Out<>();
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
//    return this.config.getSeparatePlatforms().get() ? Anchor.TOP_LEFT : Anchor.LEFT_CENTRE;
    return Anchor.TOP_LEFT;
  }

  @Override
  public void render(RenderContext context) {
//    if (!this.config.getShowLiveViewersEmitter().get()) {
//      return;
//    }

    if (!this.initialised) {
      // we can't call this in the constructor because the fontRenderer might not have been initialised yet.
      // calling this ensures that our initial size and position is correct.
      this.onShowLiveViewers(true);
      this.initialised = true;
    }

    // we have to update this constantly because of dynamic content
    // this.onResize(this.getTextWidth().times(this.scale), this.getUnscaledHeight().times(this.scale), this.getAnchor());

//    if (this.config.getSeparatePlatforms().get()) {
//      this.renderViewerCount(this.reel1, this.dimFactory.zeroGui(), this.statusService.getYoutubeLiveViewerCount());
//      this.renderViewerCount(this.reel2, this.dimFactory.fromGui(IDENTIFIED_Y_OFFSET_GUI * this.scale), this.statusService.getTwitchLiveViewerCount());
//    } else {
      this.renderViewerCount(this.reel1, this.dimFactory.zeroGui(), this.statusService.getTotalLiveViewerCount());
//    }
  }

  private void renderViewerCount(DigitReel reel, Dim yOffset, @Nullable Integer viewCount) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(this.getX().getGui(), this.getY().plus(yOffset).getGui(), 0);
    GlStateManager.scale(this.scale, this.scale, 1);

    String text = this.getText(viewCount);
    if (viewCount == null || viewCount > MAX_REEL_VALUE) {
      this.fontEngine.drawString(text, 0, 0, this.indicatorFont);
    } else {
      Dim digitWidth = this.fontEngine.getCharWidth('0');
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

    this.reel1 = new DigitReel(minecraft, dimFactory, this.fontEngine);
    this.reel2 = new DigitReel(minecraft, dimFactory, this.fontEngine);
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
    String text1, text2;
//    if (this.config.getSeparatePlatforms().get()) {
      text1 = this.getText(this.statusService.getYoutubeLiveViewerCount());
      text2 = this.getText(this.statusService.getTwitchLiveViewerCount());
//    } else {
//      text1 = this.getText(this.statusService.getTotalLiveViewerCount());
//      text2 = "";
//    }
    return this.dimFactory.fromGui(Math.max(this.fontEngine.getStringWidth(text1), this.fontEngine.getStringWidth(text2)));
  }

  private Dim getTextHeight() {
    return this.dimFactory.fromGui(this.fontEngine.FONT_HEIGHT);
  }

  private String getText(@Nullable Integer viewCount) {
    if (viewCount == null) {
      return "n/a";
    } else {
      return String.format("%02d", viewCount);
    }
  }

  private Dim getUnscaledHeight() {
    Dim textHeight = this.getTextHeight();
//    if (this.config.getSeparatePlatforms().get()) {
      return textHeight.plus(this.dimFactory.fromGui(IDENTIFIED_Y_OFFSET_GUI));
//    } else {
//      return textHeight;
//    }
  }
}
