package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.Interactive.RendererHelpers;
import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
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

  private final FontRenderer font;

  private final DimFactory dimFactory;
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

  public TitleComponent(DimFactory dimFactory, Minecraft minecraft, Dim x, Dim y, float scale, boolean canRescale, boolean canTranslate) {
    this.dimFactory = dimFactory;
    this.font = minecraft.fontRendererObj;
    this.x = x;
    this.y = y;
    this.scale = scale;
    this.canRescale = canRescale;
    this.canTranslate = canTranslate;

    this.yOffsetTitle = dimFactory.fromGui(-10 * this.mainTitleScale);
    this.yOffsetSubtitle = dimFactory.fromGui(5 * this.subTitleScale);
  }

  @Override
  public Dim getX() {
    return this.x;
  }

  @Override
  public Dim getY() {
    return this.y;
  }

  @Override
  public Dim getWidth() {
    

    return this.dimFactory.fromGui(this.texture.width * this.scale);
  }

  @Override
  public Dim getHeight() {
    Dim top = this.x.plus(this.dimFactory.fromGui(this.yOffsetTitle + this.mainTitleScale))
    return this.dimFactory.fromGui(this.texture.height * this.scale);
  }

  @Override
  public boolean canResizeBox() {
    // false because it would distort the texture
    return false;
  }

  @Override
  public void onResize(Dim newWidth, Dim newHeight, Anchor keepCentred) { }

  @Override
  public boolean canRescaleContent() {
    return this.canRescale;
  }

  @Override
  public void onRescaleContent(float newScale) {
    if (this.canRescale && this.scale != newScale) {
      float deltaScale = newScale - this.scale;
      Dim deltaWidth = this.dimFactory.fromScreen(deltaScale * this.texture.width);
      Dim deltaHeight = this.dimFactory.fromScreen(deltaScale * this.texture.height);
      this.scale = newScale;
      this.x = this.x.plus(deltaWidth.over(2));
      this.y = this.y.plus(deltaHeight.over(2));
    }
  }

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
      this.x = newX;
      this.y = newY;
    }
  }

  /** Clears the title and returns the component to its initial state. */
  public void reset() {
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
      this.reset();
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
    TitleProgress progress = this.getProgress();
    if (progress.phase == TitlePhase.HIDDEN) {
      this.reset();
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
      DimPoint centre = this.dimFactory.getMinecraftRect().getCentre();
      int alphaComponent = alpha << 24 & -16777216;
      int colour = 16777215 | alphaComponent;

      RendererHelpers.withTranslation(centre, () -> {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

        GlStateManager.pushMatrix();
        GlStateManager.scale(this.mainTitleScale, this.mainTitleScale, this.mainTitleScale);
        float titleWidth = this.font.getStringWidth(this.title);
        this.font.drawString(this.title, -titleWidth / 2, this.yOffsetTitle, colour, true);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scale(this.subTitleScale, this.subTitleScale, this.subTitleScale);
        float subtitleWidth = this.font.getStringWidth(this.subTitle);
        this.font.drawString(this.subTitle, -subtitleWidth / 2, this.yOffsetSubtitle, colour, true);
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
      });
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
