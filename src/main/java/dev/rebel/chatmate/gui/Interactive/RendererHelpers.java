package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.gui.models.DimRect;
import dev.rebel.chatmate.services.events.models.MouseEventData;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.util.Color;

import javax.annotation.Nullable;

public class RendererHelpers {
  public static void withTranslation(DimPoint translation, Runnable onRender) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(translation.getX().getGui(), translation.getY().getGui(), 0);
    onRender.run();
    GlStateManager.popMatrix();
  }

  /** Draws a coloured rect with an optional top-to-bottom gradient, and an optional exterior border (default black) */
  public static void renderRect(DimRect rect, Colour topColour, @Nullable Colour bottomColour, @Nullable Dim borderWidth, @Nullable Colour borderTopColour, @Nullable Colour borderBottomColour) {
    float x = rect.getX().getGui();
    float y = rect.getY().getGui();
    float w = rect.getWidth().getGui();
    float h = rect.getHeight().getGui();
    int zLevel = 0;

    if (bottomColour == null) {
      bottomColour = topColour;
    }

    drawGradientRect(zLevel, x, y, w - x, y + h, topColour.toSafeInt(), bottomColour.toSafeInt());

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
