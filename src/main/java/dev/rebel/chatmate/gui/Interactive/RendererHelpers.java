package dev.rebel.chatmate.gui.Interactive;

import dev.rebel.chatmate.Asset.Texture;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.Layout.RectExtension;
import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.*;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.services.util.TextHelpers;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_POLYGON;
import static org.lwjgl.opengl.GL11.GL_QUADS;

public class RendererHelpers {
  public static void withTranslation(DimPoint translation, Runnable onRender) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(translation.getX().getGui(), translation.getY().getGui(), 0);
    onRender.run();
    GlStateManager.popMatrix();
  }

  /** Maps the render space such that (0, 0) will correspond to the given translation. onRender should only use relative coordinates to the translated space. */
  public static void withMapping(DimPoint translation, float scale, Runnable onRender) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(translation.getX().getGui(), translation.getY().getGui(), 0);
    GlStateManager.scale(scale, scale, 1);
    onRender.run();
    GlStateManager.popMatrix();
  }

  /** Draws a coloured rect. */
  public static void drawRect(int zLevel, DimRect rect, Colour colour) {
    drawRect(zLevel, rect, colour, null, null);
  }

  /** Draws a coloured rect with single-coloured border. */
  public static void drawRect(int zLevel, DimRect rect, Colour colour, @Nullable Dim borderWidth, @Nullable Colour borderColour) {
    drawRect(zLevel, rect, colour, borderWidth, borderColour, borderColour, null, null, null);
  }

  /** Draws a coloured rect with single-coloured border and rounded corners. */
  public static void drawRect(int zLevel, DimRect rect, Colour colour, @Nullable Dim borderWidth, @Nullable Colour borderColour, @Nullable Dim cornerRadius) {
    drawRect(zLevel, rect, colour, borderWidth, borderColour, borderColour, cornerRadius, null, null);
  }

  /** Draws a coloured rect with single-coloured border, rounded corners, and a shadow. */
  public static void drawRect(int zLevel, DimRect rect, Colour colour, @Nullable Dim borderWidth, @Nullable Colour borderColour, @Nullable Dim cornerRadius, @Nullable Dim shadowDistance, @Nullable Colour shadowColour) {
    drawRect(zLevel, rect, colour, borderWidth, borderColour, borderColour, cornerRadius, shadowDistance, shadowColour);
  }

  /** Draws a coloured rect with many available options. */
  public static void drawRect(int zLevel, DimRect rect, Colour topColour, @Nullable Dim borderWidth, @Nullable Colour borderInnerColour, @Nullable Colour borderOuterColour, @Nullable Dim cornerRadius, @Nullable Dim shadowDistance, @Nullable Colour shadowColour) {
    drawGradientRect(zLevel, rect, topColour, topColour, GradientDirection.VERTICAL, cornerRadius);

    // draw shadow before border to give the border a chance to overlap
    if (shadowDistance != null && shadowDistance.getGui() > 0) {
      Dim shadowCornerRadius = cornerRadius;
      DimRect shadowRect = rect;
      if (borderWidth != null) {
        shadowRect = new RectExtension(borderWidth).applyAdditive(rect);
        if (cornerRadius != null) {
          shadowCornerRadius = cornerRadius.plus(borderWidth);
        }
      }

      if (Dim.positive(shadowCornerRadius)) {
        // make sure shadow is completely flush
        shadowRect = new RectExtension(cornerRadius.setScreen(1)).applySubtractive(shadowRect);
      }

      if (shadowColour == null) {
        shadowColour = Colour.BLACK;
      }
      drawRectOutline(zLevel, shadowRect, shadowDistance, shadowColour.withAlpha(0.5f), shadowColour.withAlpha(0), shadowCornerRadius);
    }

    if (borderWidth != null && borderWidth.getGui() > 0) {
      if (borderInnerColour == null) {
        borderInnerColour = new Colour(Color.BLACK);
      }
      if (borderOuterColour == null) {
        borderOuterColour = borderInnerColour;
      }

      drawRectOutline(zLevel, rect, borderWidth, borderInnerColour, borderOuterColour, cornerRadius);
    }
  }

  public static void drawRectOutline(int zLevel, DimRect rect, Dim borderWidth, Colour borderInnerColour, Colour borderOuterColour, @Nullable Dim cornerRadius) {
    Dim x = rect.getX();
    Dim y = rect.getY();
    Dim right = rect.getRight();
    Dim bottom = rect.getBottom();

    if (cornerRadius == null || cornerRadius.getGui() <= 0) {
      drawGradientRect(zLevel, x.minus(borderWidth), y.minus(borderWidth), x, bottom.plus(borderWidth), borderOuterColour, borderInnerColour, GradientDirection.HORIZONTAL, null); // left
      drawGradientRect(zLevel, right, y.minus(borderWidth), right.plus(borderWidth), bottom.plus(borderWidth), borderInnerColour, borderOuterColour, GradientDirection.HORIZONTAL, null); // right
      drawGradientRect(zLevel, x.minus(borderWidth), y.minus(borderWidth), right.plus(borderWidth), y, borderInnerColour, borderInnerColour, GradientDirection.VERTICAL, null); // top
      drawGradientRect(zLevel, x.minus(borderWidth), bottom, right.plus(borderWidth), bottom.plus(borderWidth), borderOuterColour, borderOuterColour, GradientDirection.VERTICAL, null); // bottom
    } else {
      drawGradientRect(zLevel, x.minus(borderWidth), y.plus(cornerRadius), x, bottom.minus(cornerRadius), borderOuterColour, borderInnerColour, GradientDirection.HORIZONTAL, null); // left
      drawGradientRect(zLevel, right, y.plus(cornerRadius), right.plus(borderWidth), bottom.minus(cornerRadius), borderInnerColour, borderOuterColour, GradientDirection.HORIZONTAL, null); // right
      drawGradientRect(zLevel, x.plus(cornerRadius), y.minus(borderWidth), right.minus(cornerRadius), y, borderOuterColour, borderInnerColour, GradientDirection.VERTICAL, null); // top
      drawGradientRect(zLevel, x.plus(cornerRadius), bottom, right.minus(cornerRadius), bottom.plus(borderWidth), borderInnerColour, borderOuterColour, GradientDirection.VERTICAL, null); // bottom

      float pi = (float)Math.PI;
      DimRect reduced = new RectExtension(cornerRadius).applySubtractive(rect);
      drawPartialCircle(zLevel, reduced.getTopLeft(), cornerRadius, cornerRadius.plus(borderWidth), pi / 2, pi, borderInnerColour, borderOuterColour);
      drawPartialCircle(zLevel, reduced.getTopRight(), cornerRadius, cornerRadius.plus(borderWidth), 0, pi / 2, borderInnerColour, borderOuterColour);
      drawPartialCircle(zLevel, reduced.getBottomLeft(), cornerRadius, cornerRadius.plus(borderWidth), pi, pi + pi / 2, borderInnerColour, borderOuterColour);
      drawPartialCircle(zLevel, reduced.getBottomRight(), cornerRadius, cornerRadius.plus(borderWidth), pi + pi / 2, pi + pi, borderInnerColour, borderOuterColour);
    }
  }

    /** Assumes that the cutout rect is strictly contained by the main rect. */
  public static void renderRectWithCutout(int zLevel, DimRect mainRect, DimRect cutoutRect, Colour colour, @Nullable Dim borderWidth, @Nullable Colour borderColour) {
    // ----
    // |  |
    // ----

    drawRect(zLevel, new DimRect(mainRect.getX(), mainRect.getY(), mainRect.getWidth(), cutoutRect.getY().minus(mainRect.getY())), colour); // top
    drawRect(zLevel, new DimRect(mainRect.getX(), cutoutRect.getBottom(), mainRect.getWidth(), mainRect.getBottom().minus(cutoutRect.getBottom())), colour); // bottom
    drawRect(zLevel, new DimRect(mainRect.getX(), cutoutRect.getY(), cutoutRect.getX().minus(mainRect.getX()), cutoutRect.getHeight()), colour); // left
    drawRect(zLevel, new DimRect(cutoutRect.getRight(), cutoutRect.getY(), mainRect.getRight().minus(cutoutRect.getRight()), cutoutRect.getHeight()), colour); // right
    drawRectOutline(zLevel, mainRect, borderWidth, borderColour, borderColour, null);
  }

  /** Stolen from GUI. */
  public static void drawTexturedModalRect(DimRect rect, int zLevel, TextureAtlasSprite sprite) {
    float x = rect.getX().getGui();
    float y = rect.getY().getGui();
    float width = rect.getWidth().getGui();
    float height = rect.getHeight().getGui();

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    worldRenderer.pos(x, y, zLevel).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
    worldRenderer.pos(x + width, y + height, zLevel).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
    worldRenderer.pos(x + width, y, zLevel).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
    worldRenderer.pos(x, y, zLevel).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
    tessellator.draw();
  }

  public static void drawTextureCentred(TextureManager textureManager, DimFactory dimFactory, Texture texture, DimPoint centre, float scale, @Nullable Colour colour) {
    DimPoint topLeft = new DimPoint(
        centre.getX().minus(dimFactory.fromGui(texture.width / 2.0f * scale)),
        centre.getY().minus(dimFactory.fromGui(texture.height / 2.0f * scale))
    );
    drawTexture(textureManager, dimFactory, texture, topLeft, scale, colour);
  }

  public static void drawTexture(TextureManager textureManager, DimFactory dimFactory, Texture texture, DimPoint topLeft, float scale, @Nullable Colour colour) {
    // Minecraft expects a 256x256 texture to render.
    // if we provide it with a smaller size, it will stretch out the texture.
    // so we let it do that, and simply scale the screen. This means we will need to re-calculate the position.
    // this will always draw the top-left corner at x-y, and display the (possibly rescaled) texture.
    // (admittedly I don't fully understand why this works)
    float scaleX = (float)texture.width / 256 * scale;
    float scaleY = (float)texture.height / 256 * scale;
    int u = 0, v = 0; // offset, with repeating boundaries in the 256x256 box

    // position and size in the transformed (scaled) space
    Dim renderX = topLeft.getX().over(scaleX);
    Dim renderY = topLeft.getY().over(scaleY);
    int width = 256;
    int height = 256;

    // note: it is important to use GlStateManager for two reasons:
    // 1. better efficiency
    // 2. calling the manager is used to keep track of state changes independently to GL11.* calls, and minecraft
    //    uses the state manager - thus we would get unexpected behaviour if we use GL11 methods.
    // see https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/2231761-opengl-calls-glstatemanager
    GlStateManager.pushMatrix();
    GlStateManager.scale(scaleX, scaleY, 1);
    GlStateManager.enableBlend();

    // The following are required to prevent the rendered context menu from interfering with the status indicator colour..
    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    GlStateManager.disableLighting();

    if (colour != null) {
      GlStateManager.color(colour.red / 255.0f, colour.green / 255.0f, colour.blue / 255.0f, colour.alpha / 255.0f);
    }

    textureManager.bindTexture(texture.resourceLocation);
    DimRect rect = new DimRect(renderX, renderY, dimFactory.fromGui(width), dimFactory.fromGui(height));
    int zIndex = 0;
    drawTexturedModalRect(rect, zIndex, u, v);

    GlStateManager.popMatrix();
  }

  // stolen from Gui::drawTexturedModalRect
  public static void drawTexturedModalRect(DimRect rect, int z, int u, int v) {
    float x = rect.getX().getGui();
    float y = rect.getY().getGui();
    float width = rect.getWidth().getGui();
    float height = rect.getHeight().getGui();

    float a = 0.00390625F; // 1/256
    float b = 0.00390625F;
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer renderer = tessellator.getWorldRenderer();
    renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    renderer.pos(x, y + height, z).tex(u * a, (v + height) * b).endVertex();
    renderer.pos(x + width, y + height, z).tex((u + width) * a, (v + height) * b).endVertex();
    renderer.pos(x + width, y, z).tex((u + width) * a, v * b).endVertex();
    renderer.pos(x, y, z).tex(u * a, v * b).endVertex();
    tessellator.draw();
  }

  public static void drawTooltip(DimFactory df, FontEngine font, DimPoint mousePos, String tooltip) {
    Dim screenWidth = df.getMinecraftSize().getX();
    RectExtension padding = new RectExtension(df.fromGui(3));
    List<String> lines = TextHelpers.splitText(tooltip, (int)screenWidth.over(2).minus(padding.getExtendedWidth()).getGui(), font);
    Dim contentWidth = Dim.max(Collections.map(lines, txt -> df.fromGui(font.getStringWidth(txt))));
    Dim contentHeight = df.fromGui(font.FONT_HEIGHT * lines.size());
    DimRect contentRect = new DimRect(df.zeroGui(), df.zeroGui(), contentWidth, contentHeight);
    DimRect rect = padding.applyAdditive(contentRect);

    // apply transformation
    boolean left = screenWidth.over(2).lt(mousePos.getX()); // whether to display tooltip to the left of the mouse
    DimPoint mouseOffset = new DimPoint(df.fromScreen(left ? -4 : 4), df.fromScreen(-4));
    rect = rect.withTranslation(new DimPoint(df.zeroGui(), rect.getHeight().times(-1)))
        .withTranslation(mousePos)
        .withTranslation(mouseOffset);
    if (left) {
      rect = rect.withTranslation(new DimPoint(rect.getWidth().times(-1), df.zeroGui()));
    }
    contentRect = padding.applySubtractive(rect);

    // render background
    Colour colour = new Colour(0xF0100010);
    Dim borderSize = df.fromGui(1);
    Colour borderColour = new Colour(0x505000FF);
    Dim cornerRadius = df.fromScreen(4);
    drawRect(300, rect, colour, borderSize, borderColour, cornerRadius);

    // render text
    int y = 0;
    for (String line : lines) {
      font.drawString(line, contentRect.getX().getGui(), contentRect.getY().getGui() + y, new Font().withColour(Colour.WHITE));
      y += font.FONT_HEIGHT;
    }
  }

  /** The drawn line is centred about the mathematical line of connecting `from` and `to`. If includeCaps is true, the width will extend beyond the coordinates. */
  public static void drawLine(int zLevel, Line line, Dim lineWidth, Colour startColour, Colour endColour, boolean includeCaps) {
    List<Line> linesToDraw = line.getOutline(lineWidth, includeCaps);

    GlStateManager.pushMatrix();
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GlStateManager.depthMask(false);

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

    int i = 0;
    for (Line l : linesToDraw) {
      Colour colour = i < 2 ? startColour : endColour;
      worldrenderer.pos(l.from.getX().getGui(), l.from.getY().getGui(), zLevel).color(colour.red, colour.green, colour.blue, colour.alpha).endVertex();
      i++;
    }
    tessellator.draw();

    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.popMatrix();
  }

  /** Inner radius may be zero. Radians delta must be strictly positive. At the moment, attempting to draw a complete circle will result in undefined behaviour. */
  public static void drawPartialCircle(int zLevel, DimPoint centre, Dim innerRadius, Dim outerRadius, float radiansStart, float radiansEnd, Colour innerColour, Colour outerColour) {
    List<Line> outerLines = getPartialCircleArcLines(centre, outerRadius, radiansStart, radiansEnd, null); // counter-clockwise
    List<Line> innerLines = getPartialCircleArcLines(centre, innerRadius, radiansStart, radiansEnd, outerLines.size()); // counter-clockwise
    if (outerLines.size() == 0) {
      return;
    }

    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GlStateManager.disableAlpha();
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false); // this ensures we can draw multiple transparent things on top of each other
    GlStateManager.shadeModel(7425); // for being able to draw colour gradients

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    if (innerLines.size() == 0) {
      // special case: solid section of a circle
      // this is a convex polygon, whose outline we can simply define as one shape
      worldrenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
      addVertex(worldrenderer, zLevel, centre, innerColour);
      for (Line line : outerLines) {
        addVertex(worldrenderer, zLevel, line.from, outerColour);
      }
      addVertex(worldrenderer, zLevel, Collections.last(outerLines).to, outerColour);
      tessellator.draw();

    } else {
      // there is a 1-to-1 map between the outer and inner lines, so draw a bunch of quads
      for (int i = 0; i < outerLines.size(); i++) {
        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addVertex(worldrenderer, zLevel, innerLines.get(i).from, innerColour);
        addVertex(worldrenderer, zLevel, outerLines.get(i).from, outerColour);
        addVertex(worldrenderer, zLevel, outerLines.get(i).to, outerColour);
        addVertex(worldrenderer, zLevel, innerLines.get(i).to, innerColour);
        tessellator.draw();
      }
    }

    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.shadeModel(7424);
    GlStateManager.popMatrix();
  }

  /** If numLines is not provided, uses the best number of lines to give a smooth curvature. */
  private static List<Line> getPartialCircleArcLines(DimPoint centre, Dim radius, float radiansStart, float radiansEnd, @Nullable Integer numLines) {
    if (radius.getGui() <= 0) {
      return new ArrayList<>();
    }

    float pi = (float)Math.PI;
    float radiansDelta = radiansEnd - radiansStart;
    if (radiansDelta <= 0) {
      throw new RuntimeException(String.format("Cannot draw partial circle because radiansEnd (%f) must be greater than radiansStart (%f)", radiansEnd, radiansStart));
    } else {
      while (radiansDelta > 2 * pi) {
        radiansDelta -= 2 * pi;
      }
    }
    Dim arcLength = radius.times(2).times(pi).times(radiansDelta / (2 * pi));

    // to make it look smooth, draw as many lines as required so that the maximum line length does not exceed this value
    Dim maxLength = arcLength.setScreen(5);
    int requiredLines = numLines == null ? (int)Math.ceil(arcLength.over(maxLength)) : numLines;
    float radiansPerLine = radiansDelta / requiredLines;

    List<Line> arcLines = new ArrayList<>();
    for (int i = 0; i < requiredLines; i++) {
      float thetaFrom = radiansStart + radiansPerLine * i;
      float thetaTo = thetaFrom + radiansPerLine;

      float x1 = (float)Math.cos(thetaFrom);
      float x2 = (float)Math.cos(thetaTo);
      float y1 = -(float)Math.sin(thetaFrom); // negative because y is inverted
      float y2 = -(float)Math.sin(thetaTo);

      Line line = new Line(
          new DimPoint(centre.getX().plus(radius.times(x1)), centre.getY().plus(radius.times(y1))),
          new DimPoint(centre.getX().plus(radius.times(x2)), centre.getY().plus(radius.times(y2)))
      );
      arcLines.add(line);
    }

    return arcLines;
  }

  private static void drawGradientRect(int zLevel, Dim left, Dim top, Dim right, Dim bottom, Colour startColour, Colour endColour, GradientDirection direction, @Nullable Dim cornerRadius) {
    drawGradientRect(zLevel, new DimRect(left, top, right.minus(left), bottom.minus(top)), startColour, endColour, direction, cornerRadius);
  }

  /** Stolen from GuiUtils (and amusingly also Gui.java), but modified for float coords and more colours. */
  private static void drawGradientRect(int zLevel, DimRect rect, Colour startColour, Colour endColour, GradientDirection direction, @Nullable Dim cornerRadius)
  {
    // 2 1
    // 3 4
    Colour colour1 = direction == GradientDirection.VERTICAL ? startColour : endColour;
    Colour colour2 = startColour;
    Colour colour3 = direction == GradientDirection.VERTICAL ? endColour : startColour;
    Colour colour4 = endColour;

    GlStateManager.pushMatrix();
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GlStateManager.disableAlpha();
    GlStateManager.disableTexture2D();
    GlStateManager.depthMask(false); // this ensures we can draw multiple transparent things on top of each other
    GlStateManager.shadeModel(GL11.GL_SMOOTH); // for being able to draw colour gradients

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    worldRenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION_COLOR); // the final shape is always a convex polygon, so we can draw it in one go

    if (cornerRadius == null || cornerRadius.getGui() <= 0) {
      addVertex(worldRenderer, zLevel, rect.getTopRight(), colour1);
      addVertex(worldRenderer, zLevel, rect.getTopLeft(), colour2);
      addVertex(worldRenderer, zLevel, rect.getBottomLeft(), colour3);
      addVertex(worldRenderer, zLevel, rect.getBottomRight(), colour4);
    } else {
      // almost too easy - just concatenate all the partial circles!
      // the colour gradient is not 100% correct, but for small corner radii it shouldn't be noticeable.
      // if this becomes a problem in the future, will need to do some colour lerping.
      DimRect innerRect = new RectExtension(cornerRadius).applySubtractive(rect);
      float radians = 0;
      float piOver2 = (float)Math.PI / 2;

      List<DimPoint> centres = Collections.list(innerRect.getTopRight(), innerRect.getTopLeft(), innerRect.getBottomLeft(), innerRect.getBottomRight());
      List<Colour> colours = Collections.list(colour1, colour2, colour3, colour4);
      for (int c = 0; c < centres.size(); c++) {
        Colour colour = colours.get(c);

        Line prevLine = null;
        for (Line line : getPartialCircleArcLines(centres.get(c), cornerRadius, radians, radians + piOver2, null)) {
          if (prevLine == null) {
            // also draw the "from" point only for the first time
            addVertex(worldRenderer, zLevel, line.from, colour);
          }
          addVertex(worldRenderer, zLevel, line.to, colour);
          prevLine = line;
        }

        radians += piOver2;
      }
    }

    tessellator.draw();

    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.depthMask(true);
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }

  private static void addVertex(WorldRenderer worldRenderer, int zLevel, DimPoint point, Colour colour) {
    worldRenderer.pos(point.getX().round().getGui(), point.getY().round().getGui(), zLevel).color(colour.red / 255.0f, colour.green / 255.0f, colour.blue / 255.0f, colour.alpha / 255.0f).endVertex();
  }

  private enum GradientDirection {
    HORIZONTAL, VERTICAL
  }
}
