package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.gui.models.Line;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.util.Color;

import javax.annotation.Nullable;
import java.util.List;

public class RendererHelpers {
  public static void withTranslation(DimPoint translation, Runnable onRender) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(translation.getX().getGui(), translation.getY().getGui(), 0);
    onRender.run();
    GlStateManager.popMatrix();
  }

  /** Draws a coloured rect. */
  public static void renderRect(int zLevel, DimRect rect, Colour colour) {
    renderRect(zLevel, rect, colour, null, null, null, null);
  }

  /** Draws a coloured rect with an optional top-to-bottom gradient, and an optional exterior border (default black) */
  public static void renderRect(int zLevel, DimRect rect, Colour topColour, @Nullable Colour bottomColour, @Nullable Dim borderWidth, @Nullable Colour borderTopColour, @Nullable Colour borderBottomColour) {
    float x = rect.getX().getGui();
    float y = rect.getY().getGui();
    float w = rect.getWidth().getGui();
    float h = rect.getHeight().getGui();

    if (bottomColour == null) {
      bottomColour = topColour;
    }

    drawGradientRect(zLevel, x, y, x + w, y + h, topColour.toSafeInt(), bottomColour.toSafeInt());

    if (borderWidth != null) {
      if (borderTopColour == null) {
        borderTopColour = new Colour(Color.BLACK);
      }
      if (borderBottomColour == null) {
        borderBottomColour = borderTopColour;
      }
      final int borderColorStart = borderTopColour.toSafeInt();
      final int borderColorEnd = borderBottomColour.toSafeInt();
      float thickness = borderWidth.getGui();

      drawGradientRect(zLevel, x - thickness, y - thickness, x, y + h + thickness, borderColorStart, borderColorEnd); // left
      drawGradientRect(zLevel, x + w, y - thickness, x + w + thickness, y + h + thickness, borderColorStart, borderColorEnd); // right
      drawGradientRect(zLevel, x - thickness, y - thickness, x + w + thickness, y, borderColorStart, borderColorStart); // top
      drawGradientRect(zLevel, x - thickness, y + h, x + w + thickness, y + h + thickness, borderColorEnd, borderColorEnd); // bottom
    }
  }

  /** Assumes that the cutout rect is strictly contained by the main rect. */
  public static void renderRectWithCutout(int zLevel, DimRect mainRect, DimRect cutoutRect, Colour colour) {
    // ----
    // |  |
    // ----

    renderRect(zLevel, new DimRect(mainRect.getX(), mainRect.getY(), mainRect.getWidth(), cutoutRect.getY().minus(mainRect.getY())), colour); // top
    renderRect(zLevel, new DimRect(mainRect.getX(), cutoutRect.getBottom(), mainRect.getWidth(), mainRect.getBottom().minus(cutoutRect.getBottom())), colour); // bottom
    renderRect(zLevel, new DimRect(mainRect.getX(), cutoutRect.getY(), cutoutRect.getX().minus(mainRect.getX()), cutoutRect.getHeight()), colour); // left
    renderRect(zLevel, new DimRect(cutoutRect.getRight(), cutoutRect.getY(), mainRect.getRight().minus(cutoutRect.getRight()), cutoutRect.getHeight()), colour); // right
  }

  /** Stolen from GUI. */
  public static void drawTexturedModalRect(DimRect rect, int zLevel, TextureAtlasSprite sprite) {
    float x = rect.getX().getGui();
    float y = rect.getY().getGui();
    float width = rect.getWidth().getGui();
    float height = rect.getHeight().getGui();

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
    worldRenderer.pos(x, y, zLevel).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
    worldRenderer.pos(x + width, y + height, zLevel).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
    worldRenderer.pos(x + width, y, zLevel).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
    worldRenderer.pos(x, y, zLevel).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
    tessellator.draw();
  }

  public static void drawTexturedModalRect(DimRect rect, int zLevel, int u, int v) {
    float x = rect.getX().getGui();
    float y = rect.getY().getGui();
    float width = rect.getWidth().getGui();
    float height = rect.getHeight().getGui();

    float a = 0.00390625F;
    float b = 0.00390625F;
    Tessellator lvt_9_1_ = Tessellator.getInstance();
    WorldRenderer lvt_10_1_ = lvt_9_1_.getWorldRenderer();
    lvt_10_1_.begin(7, DefaultVertexFormats.POSITION_TEX);
    lvt_10_1_.pos(x, y + height, zLevel).tex(u * a, (v + height) * b).endVertex();
    lvt_10_1_.pos(x + width, y + height, zLevel).tex((u + width) * a, (v + height) * b).endVertex();
    lvt_10_1_.pos(x + width, y, zLevel).tex((u + width) * a, v * b).endVertex();
    lvt_10_1_.pos(x, y, zLevel).tex(u * a, v * b).endVertex();
    lvt_9_1_.draw();
  }


  /** The drawn line is centred about the mathematical line of connecting `from` and `to`. If includeCaps is true, the width will extend beyond the coordinates. */
  public static void drawLine(Line line, Dim lineWidth, Colour colour, boolean includeCaps) {
    int zLevel = 0;
    int red = colour.red;
    int green = colour.green;
    int blue = colour.blue;
    int alpha = colour.alpha;

    List<Line> linesToDraw = line.getOutline(lineWidth, includeCaps);

    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.shadeModel(7425);

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);

    for (Line l : linesToDraw) {
      worldrenderer.pos(l.from.getX().getGui(), l.from.getY().getGui(), zLevel).color(red, green, blue, alpha).endVertex();
    }
    tessellator.draw();

    GlStateManager.shadeModel(7424);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
  }

  /**
   * Stolen from GuiUtils (and amusingly also Gui.java), but modified for float coords.
   */
  private static void drawGradientRect(int zLevel, float left, float top, float right, float bottom, int startColor, int endColor)
  {
    float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
    float startRed = (float)(startColor >> 16 & 255) / 255.0F;
    float startGreen = (float)(startColor >> 8 & 255) / 255.0F;
    float startBlue = (float)(startColor & 255) / 255.0F;
    float endAlpha = (float)(endColor >> 24 & 255) / 255.0F;
    float endRed = (float)(endColor >> 16 & 255) / 255.0F;
    float endGreen = (float)(endColor >> 8 & 255) / 255.0F;
    float endBlue = (float)(endColor & 255) / 255.0F;

    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.shadeModel(7425);

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    worldrenderer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
    worldrenderer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
    worldrenderer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
    worldrenderer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
    tessellator.draw();

    GlStateManager.shadeModel(7424);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
  }
}
