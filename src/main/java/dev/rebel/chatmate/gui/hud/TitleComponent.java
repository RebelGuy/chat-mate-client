package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.GuiChatMateHudScreen;
import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.Dim.DimAnchor;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Date;

import static dev.rebel.chatmate.services.util.TextHelpers.isNullOrEmpty;
import static org.lwjgl.opengl.GL11.*;

// stolen from GuiIngame.java
/** Draws text centred at the given point using the Minecraft title style (title with sub-title). */
public class TitleComponent implements IHudComponent {
  private final float mainTitleScale = 4;
  private final float subTitleScale = 2;
  private final Dim yOffsetTitle;
  private final Dim yOffsetSubtitle;

  private final Minecraft minecraft;

  private final DimFactory dimFactory;
  // x and y are used as an anchor around which the offsets are calculated. they are NOT the top-left corner or centre of the box.
  private Dim x;
  private Dim y;
  private float scale;
  private final boolean canRescale;
  private final boolean canTranslate;

  private String title = null;
  private String subTitle = null;
  protected long fadeInTime = 0;
  protected long displayTime = 0;
  protected long fadeOutTime = 0;
  private Long startTime = null;

  public TitleComponent(DimFactory dimFactory, Minecraft minecraft, boolean canRescale, boolean canTranslate) {
    this.dimFactory = dimFactory;
    this.minecraft = minecraft;

    this.resetPositionState();
    this.canRescale = canRescale;
    this.canTranslate = canTranslate;

    this.yOffsetTitle = dimFactory.fromGui(-10 * this.mainTitleScale);
    this.yOffsetSubtitle = dimFactory.fromGui(5 * this.subTitleScale);
  }

  private void resetPositionState() {
    DimPoint centre = dimFactory.getMinecraftRect().getCentre();
    this.x = centre.getX().setAnchor(DimAnchor.GUI);
    this.y = centre.getY().setAnchor(DimAnchor.GUI);
    this.scale = 1;
  }

  @Override
  public Dim getX() {
    return this.x.minus(this.getWidth().over(2));
  }

  @Override
  public Dim getY() {
    return this.y.plus(this.yOffsetTitle);
  }

  @Override
  public Dim getWidth() {
    if (this.getProgress().phase == TitlePhase.HIDDEN) {
      return this.dimFactory.zeroGui();
    }

    FontRenderer font = this.minecraft.fontRendererObj;
    Dim titleWidth = this.dimFactory.fromGui(font.getStringWidth(this.title) * this.mainTitleScale);
    Dim subtitleWidth = this.dimFactory.fromGui(font.getStringWidth(this.subTitle) * this.subTitleScale);

    return Dim.max(titleWidth, subtitleWidth).times(this.scale);
  }

  @Override
  public Dim getHeight() {
    if (this.getProgress().phase == TitlePhase.HIDDEN) {
      return this.dimFactory.zeroGui();
    }

    Dim top = this.y.plus(this.yOffsetTitle);
    Dim bottom = this.y.plus(this.yOffsetSubtitle).plus(this.dimFactory.fromGui(this.minecraft.fontRendererObj.FONT_HEIGHT * this.subTitleScale));
    return bottom.minus(top).times(this.scale);
  }

  @Override
  public boolean canResizeBox() {
    // false because it would distort the text
    return false;
  }

  @Override
  public void onResize(Dim newWidth, Dim newHeight, Anchor keepCentred) { }

  @Override
  public boolean canRescaleContent() {
    // it's a nightmare to work out because the box is not exactly centred at (x, y)
    return false;
  }

  @Override
  public void onRescaleContent(float newScale) { }

  @Override
  public float getContentScale() {
    return this.scale;
  }

  @Override
  public boolean canTranslate() {
    return this.canTranslate;
  }

  @Override
  public void onTranslate(Dim newX, Dim newY) {
    if (this.canTranslate) {
      this.x = newX.setAnchor(DimAnchor.GUI).plus(this.getWidth().over(2));
      this.y = newY.setAnchor(DimAnchor.GUI).minus(this.yOffsetTitle);
    }
  }

  /** Clears the title and returns the component to its initial state. */
  public void clearTimerState() {
    this.title = null;
    this.subTitle = null;
    this.startTime = null;
    this.fadeInTime = 0;
    this.displayTime = 0;
    this.fadeOutTime = 0;
  }

  /** Starts displaying the title immediately. Resets the existing title, if one exists. */
  public void displayTitle(String title, String subTitle, long fadeInTime, long displayTime, long fadeOutTime) {
    if (isNullOrEmpty(title) && isNullOrEmpty(subTitle)) {
      this.resetPositionState();
      this.clearTimerState();
      return;
    }

    this.title = title;
    this.subTitle = subTitle;
    this.fadeInTime = fadeInTime;
    this.displayTime = displayTime;
    this.fadeOutTime = fadeOutTime;
    this.startTime = getTime();
  }

  @Override
  public void render(RenderContext context) {
    // don't draw if other screens are shown
    GuiScreen screen = this.minecraft.currentScreen;
    if (screen != null && !(screen instanceof GuiChatMateHudScreen)) {
      return;
    }

    TitleProgress progress = this.getProgress();
    if (progress.phase == TitlePhase.HIDDEN) {
      this.clearTimerState();
      this.resetPositionState();
      return;
    }

    int alpha;
    if (progress.phase == TitlePhase.FADE_IN) {
      alpha = (int)(progress.progress * 255);
    } else if (progress.phase == TitlePhase.FADE_OUT) {
      alpha = 255 - (int)(progress.progress * 255);
    } else {
      alpha = 255;
    }
    
    if (alpha > 4) {
      FontRenderer font = this.minecraft.fontRendererObj;
      int alphaComponent = alpha << 24 & -16777216;
      int colour = 16777215 | alphaComponent;

      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

      DimPoint titleTranslation = new DimPoint(this.x, this.y.plus(this.yOffsetTitle));
      RendererHelpers.withMapping(titleTranslation, this.mainTitleScale, () -> {
        float titleWidth = font.getStringWidth(this.title);
        font.drawStringWithShadow(this.title, -titleWidth / 2, 0, colour);
      });

      DimPoint subtitleTranslation = new DimPoint(this.x, this.y.plus(this.yOffsetSubtitle));
      RendererHelpers.withMapping(subtitleTranslation, this.subTitleScale, () -> {
        float subtitleWidth = font.getStringWidth(this.subTitle);
        font.drawStringWithShadow(this.subTitle, -subtitleWidth / 2, 0, colour);
      });

      GlStateManager.disableBlend();
    }
  }

  private TitleProgress getProgress() {
    long t = getTime();
    if (this.startTime == null) {
      return new TitleProgress(TitlePhase.HIDDEN, 0);
    }

    long endFadeIn = this.startTime + this.fadeInTime;
    long endDisplay = endFadeIn + this.displayTime;
    long endFadeOut = endDisplay + this.fadeOutTime;

    if (t < endFadeIn) {
      return new TitleProgress(TitlePhase.FADE_IN, (float)(t - this.startTime) / this.fadeInTime);
    } else if (t < endDisplay) {
      return new TitleProgress(TitlePhase.SHOWN, (float)(t - endFadeIn) / this.displayTime);
    } else if (t < endFadeOut) {
      return new TitleProgress(TitlePhase.FADE_OUT, (float)(t - endDisplay) / this.fadeOutTime);
    } else {
      return new TitleProgress(TitlePhase.HIDDEN, 0);
    }
  }

  private static long getTime() {
    return new Date().getTime();
  }

  private static class TitleProgress {
    public final TitlePhase phase;
    /** 0 <= x < 1 */
    public final float progress;

    private TitleProgress(TitlePhase phase, float progress) {
      this.phase = phase;
      this.progress = progress;
    }
  }

  private enum TitlePhase {
    HIDDEN, FADE_IN, SHOWN, FADE_OUT
  }
}
