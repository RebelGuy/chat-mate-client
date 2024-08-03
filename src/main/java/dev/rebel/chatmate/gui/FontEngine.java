package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.StateManagement.State;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import dev.rebel.chatmate.util.Collections;
import gnu.trove.stack.array.TFloatArrayStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static dev.rebel.chatmate.gui.Interactive.RendererHelpers.withAttrib;
import static dev.rebel.chatmate.gui.Interactive.RendererHelpers.withConditionalAttrib;

public class FontEngine {
  private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
  public static final char CHAR_SECTION_SIGN = 167;
  private static final char CHAR_SPACE = 32;
  private static final char CHAR_NEW_LINE = 10;
  private static final String SECTION_SIGN_STRING = "\u00A7";
  private static final String STYLE_CODES = "0123456789abcdefklmnor";
  private static final String ASCII_CHARACTERS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
  private static final float UNICODE_SCALING_FACTOR = 0.5f;

  private final DimFactory dimFactory;

  /** Array of width of all the characters in default.png */
  protected int[] charWidth = new int[256];
  /** the height in pixels of default text */
  public int FONT_HEIGHT = 9;
  public Dim FONT_HEIGHT_DIM;
  public Random fontRandom = new Random();
  /** Array of the start/end column (in upper/lower nibble) for every glyph in the /font directory. */
  protected byte[] glyphWidth = new byte[65536];
  /** Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
   * drop shadows. */
  private final int[] colorCode = new int[32];
  protected final ResourceLocation locationFontTexture;
  /** The RenderEngine used to load and setup glyph textures. */
  private final TextureManager renderEngine;
  /** If true, strings should be rendered with Unicode fonts instead of the default.png font */
  private boolean unicodeFlag;

  public FontEngine(DimFactory dimFactory, GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode) {
    this.dimFactory = dimFactory;
    this.locationFontTexture = location;
    this.renderEngine = textureManagerIn;
    this.unicodeFlag = unicode;
    this.FONT_HEIGHT_DIM = this.dimFactory.fromGui(9);

    bindTexture(this.locationFontTexture);

    // initialise preset colours (first 16), as well as their corresponding shadow colour (next 16)
    for (int i = 0; i < 32; ++i) {
      int j = (i >> 3 & 1) * 85;
      int k = (i >> 2 & 1) * 170 + j;
      int l = (i >> 1 & 1) * 170 + j;
      int i1 = (i >> 0 & 1) * 170 + j;

      if (i == 6) {
        k += 85;
      }

      if (gameSettingsIn.anaglyph) {
        int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
        int k1 = (k * 30 + l * 70) / 100;
        int l1 = (k * 30 + i1 * 70) / 100;
        k = j1;
        l = k1;
        i1 = l1;
      }

      if (i >= 16) {
        k /= 4;
        l /= 4;
        i1 /= 4;
      }

      this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
    }

    this.readFontTexture();
    this.readGlyphSizes();
  }

  public void onResourceManagerReload(IResourceManager resourceManager) {
    this.readFontTexture();
    this.readGlyphSizes();
  }

  private void readFontTexture() {
    BufferedImage bufferedimage;

    try {
      bufferedimage = TextureUtil.readBufferedImage(getResourceInputStream(this.locationFontTexture));
    } catch (IOException ioexception) {
      throw new RuntimeException(ioexception);
    }

    int i = bufferedimage.getWidth();
    int j = bufferedimage.getHeight();
    int[] aint = new int[i * j];
    bufferedimage.getRGB(0, 0, i, j, aint, 0, i);
    int k = j / 16;
    int l = i / 16;
    int i1 = 1;
    float f = 8.0F / (float)l;

    for (int j1 = 0; j1 < 256; ++j1) {
      int k1 = j1 % 16;
      int l1 = j1 / 16;

      if (j1 == 32) {
        this.charWidth[j1] = 3 + i1;
      }

      int i2;

      for (i2 = l - 1; i2 >= 0; --i2) {
        int j2 = k1 * l + i2;
        boolean flag = true;

        for (int k2 = 0; k2 < k && flag; ++k2) {
          int l2 = (l1 * l + k2) * i;

          if ((aint[j2 + l2] >> 24 & 255) != 0) {
            flag = false;
            break;
          }
        }

        if (!flag) {
          break;
        }
      }

      ++i2;
      this.charWidth[j1] = (int)(0.5D + (double)((float)i2 * f)) + i1;
    }
  }

  private void readGlyphSizes() {
    InputStream inputstream = null;

    try {
      inputstream = getResourceInputStream(new ResourceLocation("font/glyph_sizes.bin"));
      inputstream.read(this.glyphWidth);
    } catch (IOException ioexception) {
      throw new RuntimeException(ioexception);
    } finally {
      IOUtils.closeQuietly(inputstream);
    }
  }

  /** Render the given char at the position. */
  private Dim renderChar(char ch, Font font, Dim x, Dim y) {
    CharRenderer renderer = (c, isItalic, posX, posY, posZ) -> {
      if (ch == CHAR_SPACE) {
        // we don't actually render spaces, as the texture is only one unit across
        return this.dimFactory.fromGui(4);
      } else {
        return this.renderCharTexture(c, isItalic, posX, posY, posZ);
      }
    };

    // this method can probably be refactored into a much more elegant approach, but it'll do for now.
    final State<Dim> width = new State<>(this.dimFactory.zeroGui()); // hack so we can modify this inside the lambdas... thanks java

    float offsetMultiplier = this.getOffsetMultiplier(ch);
    Dim boldOffsetX = this.dimFactory.fromGui(1).times(offsetMultiplier);

    // set the main layer
    // for some reason we can't use the GlStateManager with pushAttrib(), so instead we have to use the raw GL11 API
    // https://www.cs.sfu.ca/~haoz/teaching/htmlman/pushattrib.html
    withAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT, () -> {
      GL11.glEnable(GL11.GL_ALPHA_TEST);

      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

      if (font.getSmoothFont()) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
      }

      // transparency must be extracted from the font colour and applied to the layer on which the characters are drawn,
      // otherwise we get issues where overlaps between text (e.g. shadows, bold text) causes double-transparency artifacts.
      Colour fontColour = font.getColour();

      @Nullable Shadow shadow = font.getShadow();
      withConditionalAttrib(shadow != null, GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT, () -> {

        Colour shadowColour = shadow.getColour(font);

        GL11.glColor4f(shadowColour.redf, shadowColour.greenf, shadowColour.bluef, shadowColour.alphaf);
        GlStateManager.color(shadowColour.redf, shadowColour.greenf, shadowColour.bluef, shadowColour.alphaf);

        Dim shadowX = x.plus(shadow.getOffset().getX().times(offsetMultiplier));
        Dim shadowY = y.plus(shadow.getOffset().getX().times(offsetMultiplier));
        Dim shadowWidth = this.dimFactory.zeroGui();
        shadowWidth = shadowWidth.plus(renderer.render(ch, font.getItalic(), shadowX, shadowY, 0));

        // todo: bold/shadows with partial transparency look weird because regions of the text where the multiple render passes overlap end up more opaque.
        // workaround: https://stackoverflow.com/a/32656965
        if (font.getBold()) {
          renderer.render(ch, font.getItalic(), shadowX.plus(boldOffsetX), shadowY, 0);
          shadowWidth = shadowWidth.plus(boldOffsetX);
        }

        drawStylisedArtifacts(shadowX, shadowY, shadowWidth, font);
      });

      GL11.glColor4f(fontColour.redf, fontColour.greenf, fontColour.bluef, fontColour.alphaf);

      width.setState(w -> w.plus(renderer.render(ch, font.getItalic(), x, y, 0)));

      if (font.getBold()) {
        renderer.render(ch, font.getItalic(), x.plus(boldOffsetX), y, 0);
        width.setState(w -> w.plus(boldOffsetX));
      }

      drawStylisedArtifacts(x, y, width.getState(), font);
    });

    return width.getState();
  }

  /** Returns the multiplier used for font offsets - depends on whether the character is unicode or not. */
  private float getOffsetMultiplier(char ch) {
    // the unicode scaling is applied as per the vanilla implementation, probably because the unicode characters are thinner
    int asciiIndex = ASCII_CHARACTERS.indexOf(ch);
    boolean isUnicode = asciiIndex == -1 || this.unicodeFlag;
    return isUnicode ? UNICODE_SCALING_FACTOR : 1;
  }

  /** Returns the width of the rendered texture. */
  private Dim renderCharTexture(char ch, boolean isItalic, Dim x, Dim y, float z) {
    int asciiIndex = ASCII_CHARACTERS.indexOf(ch);
    boolean isUnicode = asciiIndex == -1 || this.unicodeFlag;
    if (isUnicode) {
      return this.renderUnicodeChar(ch, isItalic, x, y, z);
    } else {
      return this.renderDefaultChar(asciiIndex, isItalic, x, y, z);
    }
  }

  /** Render a single character with the default.png font at current (posX,posY) location... */
  protected Dim renderDefaultChar(int asciiIndex, boolean isItalic, Dim xDim, Dim yDim, float z) {
    float col = (float)(asciiIndex % 16 * 8);
    float row = (float)(asciiIndex / 16 * 8);
    float italicTransform = isItalic ? 0.75f : 0;
    bindTexture(this.locationFontTexture);

    // padding helps us avoid abrupt edges of smoothed characters, and also prevents pixels of the neighbouring character in the map from showing up.
    // ensure that the same padding is applied to the texture and vertex numerators, otherwise scaling will occur.
    float paddingRight = 0.8f; // default: 1
    float paddingLeft = -0.2f; // default: 0
    float paddingBottom = 0.2f; // default: 0.1 - characters are abruptly cut off, but reducing this causes the characters in the row below will show up
    float paddingTop = 0.13f; // default: 0 - can't change because sometimes the characters from the row above can show up
    float texCharWidth = this.charWidth[asciiIndex];

    // in the future, these are some of the settings that we could add to a Font object, which acts as a preset transform of the font renderer (including colour, perhaps),
    // and which exposes methods for measuring and rendering text.
    // we will be able to set different fonts for different contexts, e.g. the menu font might be different to the chat font, etc, and ChatComponents can specify their own custom font
    float drawnScaleX = 1f; // adjusting this squishes/expands the text in the x direction, drastically changing the look of the font
    float charWidth = texCharWidth * drawnScaleX; // draws them closer together/farther apart

    float x = xDim.getGui();
    float y = yDim.getGui();
    GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
    GL11.glTexCoord2f((col + paddingLeft) / 128.0F, (row + paddingTop) / 128.0F);
    GL11.glVertex3f(x + paddingLeft + italicTransform, y + paddingTop, z);
    GL11.glTexCoord2f((col + paddingLeft) / 128.0F, (row + (8 - paddingBottom)) / 128.0F);
    GL11.glVertex3f(x + paddingLeft - italicTransform, y + (8 - paddingBottom), z);
    GL11.glTexCoord2f((col + texCharWidth - paddingRight) / 128.0F, (row + paddingTop) / 128.0F);
    GL11.glVertex3f(x + texCharWidth * drawnScaleX - paddingRight + italicTransform, y + paddingTop, z);
    GL11.glTexCoord2f((col + texCharWidth - paddingRight) / 128.0F, (row + (8 - paddingBottom)) / 128.0F);
    GL11.glVertex3f(x + texCharWidth * drawnScaleX - paddingRight - italicTransform, y + (8 - paddingBottom), z);
    GL11.glEnd();

    return this.dimFactory.fromGui(charWidth);
  }

  private ResourceLocation getUnicodePageLocation(int page) {
    if (unicodePageLocations[page] == null) {
      unicodePageLocations[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", new Object[] {Integer.valueOf(page)}));
    }

    return unicodePageLocations[page];
  }

  /** Load one of the /font/glyph_XX.png into a new GL texture and store the texture ID in glyphTextureName array. */
  private void loadGlyphTexture(int page) {
    bindTexture(this.getUnicodePageLocation(page));
  }

  /** Render a single Unicode character at current (posX,posY) location using one of the /font/glyph_XX.png files... */
  protected Dim renderUnicodeChar(char ch, boolean isItalic, Dim xDim, Dim yDim, float z) {
    if (this.glyphWidth[ch] == 0) {
      return this.dimFactory.zeroGui();
    } else {
      // this is so similar to the renderDefaultChar method, but I just can't quite get my hand on it.
      // ideally we want to generalise both methods, perhaps even represent the unicode flag in the font.
      int page = ch / 256;
      this.loadGlyphTexture(page);
      float charStart = this.glyphWidth[ch] >>> 4; // divides by 2^4 (16) and rounds down (row on page)
      float nextCharStart = 1 + (this.glyphWidth[ch] & 15); // remainder after dividing by 16 (column on page)

      float col = (float)(ch % 16 * 16) + charStart;
      float row = (float)((ch & 255) / 16 * 16);
      float texCharWidth = nextCharStart - charStart - 0.02F;
      float italicTransform = isItalic ? 1.0F : 0.0F;
      float x = xDim.getGui();
      float y = yDim.getGui();
      GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
      GL11.glTexCoord2f(col / 256.0F, row / 256.0F);
      GL11.glVertex3f(x + italicTransform, y, 0.0F);
      GL11.glTexCoord2f(col / 256.0F, (row + 15.98F) / 256.0F);
      GL11.glVertex3f(x - italicTransform, y + 7.99F, 0.0F);
      GL11.glTexCoord2f((col + texCharWidth) / 256.0F, row / 256.0F);
      GL11.glVertex3f(x + texCharWidth * UNICODE_SCALING_FACTOR + italicTransform, y, 0.0F);
      GL11.glTexCoord2f((col + texCharWidth) / 256.0F, (row + 15.98F) / 256.0F);
      GL11.glVertex3f(x + texCharWidth * UNICODE_SCALING_FACTOR - italicTransform, y + 7.99F, 0.0F);
      GL11.glEnd();
      return this.dimFactory.fromGui(nextCharStart - charStart).times(UNICODE_SCALING_FACTOR).plus(this.dimFactory.fromGui(1));
    }
  }

  public float drawString(String text, float x, float y, Font baseFont) {
    return this.drawString(text, this.dimFactory.fromGui(x), this.dimFactory.fromGui(y), baseFont).getGui();
  }

  /** Returns the new x-position of the cursor. */
  public Dim drawString(List<Tuple2<String, Font>> formattedText, Dim x, Dim y, boolean renderSectionCharacter) {
    for (Tuple2<String, Font> text : formattedText) {
      x = this.drawString(text._1, x, y, text._2, renderSectionCharacter);
    }
    return x;
  }

  /** Render a single line string at the given position, respecting the styles contained in the string. Returns the new x-position of the cursor. Formatting codes are hidden. */
  public Dim drawString(String text, Dim x, Dim y, Font baseFont) {
    return this.drawString(text, x, y, baseFont, false);
  }

  /** Adds the style represented by the `formattingCode` to the given `font`. If the style is to be reset, `baseFont` will be returned. */
  public Font getFontFromChar(char formattingCode, Font baseFont, Font font) {
    int styleIndex = STYLE_CODES.indexOf(formattingCode);
    if (styleIndex >= 0 && styleIndex < 16) {
      int colourInt = this.colorCode[styleIndex];
      Colour colour = new Colour(colourInt).withAlpha(baseFont.getColour().alpha);

      // todo: it seems weird that colours must go first, else the stylings are reset, but it seems that this is the vanilla behaviour. double check that it makes sense, otherwise don't reset the stylings.
      font = font
          .withObfuscated(false)
          .withBold(false)
          .withItalic(false)
          .withUnderlined(false)
          .withStrikethrough(false)
          .withColour(colour);
    } else if (styleIndex == 16) {
      font = font.withObfuscated(true);
    } else if (styleIndex == 17) {
      font = font.withBold(true);
    } else if (styleIndex == 18) {
      font = font.withStrikethrough(true);
    } else if (styleIndex == 19) {
      font = font.withUnderlined(true);
    } else if (styleIndex == 20) {
      font = font.withItalic(true);
    } else if (styleIndex == 21) {
      font = baseFont;
    }

    return font;
  }

  /** Render a single line string at the given position, respecting the styles contained in the string. Returns the new x-position of the cursor.
   * If `renderSectionCharacter` is true, the formatting codes will be drawn. */
  public Dim drawString(String text, Dim x, Dim y, Font baseFont, boolean renderSectionCharacter) {
    if (text == null || text.length() == 0) {
      return x;
    }

    Font currentFont = baseFont;

    for (int i = 0; i < text.length(); ++i) {
      char c = text.charAt(i);

      if (c == CHAR_SECTION_SIGN && i + 1 < text.length()) {
        char formattingCode = text.toLowerCase(Locale.ENGLISH).charAt(i + 1);
        currentFont = this.getFontFromChar(formattingCode, baseFont, currentFont);

        // if instructed, render the formatting code but also apply the very style that it represents
        if (renderSectionCharacter) {
          Dim charWidth = this.renderChar(c, currentFont, x, y);
          x = x.plus(charWidth);
        } else {
          // skip the next character
          i++;
        }
      } else {
        int asciiIndex = ASCII_CHARACTERS.indexOf(c);

        // get a random character with the same width
        if (currentFont.getObfuscated() && asciiIndex != -1) {
          Dim k = this.getCharWidth(c);
          char c1;

          while (true) {
            asciiIndex = this.fontRandom.nextInt(ASCII_CHARACTERS.length());
            c1 = ASCII_CHARACTERS.charAt(asciiIndex);

            if (k.equals(this.getCharWidth(c1))) {
              break;
            }
          }

          c = c1;
        }

        Dim charWidth = this.renderChar(c, currentFont, x, y);

        x = x.plus(charWidth);
//         x += (float)((int)charWidth); // todo: why is it purposefully rounding down? don't we want to keep the precision?L
      }
    }

    return x;
  }

  protected void drawStylisedArtifacts(Dim xDim, Dim yDim, Dim widthDim, Font font) {
    float x1 = xDim.getGui();
    float x2 = xDim.plus(widthDim).getGui();

    if (font.getStrikethrough()) {
      float y = yDim.plus(this.FONT_HEIGHT_DIM.over(2)).getGui();
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      GlStateManager.disableTexture2D();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION);
      worldrenderer.pos(x1, y, 0).endVertex();
      worldrenderer.pos(x2, y, 0).endVertex();
      worldrenderer.pos(x2, y - 1, 0).endVertex();
      worldrenderer.pos(x1, y - 1, 0).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
    }

    if (font.getUnderlined()) {
      float y = yDim.plus(this.FONT_HEIGHT_DIM).getGui();
      Tessellator tessellator1 = Tessellator.getInstance();
      WorldRenderer worldrenderer1 = tessellator1.getWorldRenderer();
      GlStateManager.disableTexture2D();
      worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
      worldrenderer1.pos(x1, y, 0).endVertex();
      worldrenderer1.pos(x2, y, 0).endVertex();
      worldrenderer1.pos(x2, y - 1, 0).endVertex();
      worldrenderer1.pos(x1, y - 1, 0).endVertex();
      tessellator1.draw();
      GlStateManager.enableTexture2D();
    }
  }

  public int getStringWidth(String text) {
    return this.getStringWidth(text, false);
  }

  public int getStringWidth(String text, boolean renderSectionCharacter) {
    return (int)this.getStringWidthDim(text, renderSectionCharacter).getGui();
  }

  public Dim getStringWidthDim(String text) {
    return this.getStringWidthDim(text, new Font());
  }

  public Dim getStringWidthDim(String text, Font font) {
    return this.getStringWidthDim(text, font, false);
  }

  public Dim getStringWidthDim(String text, boolean renderSectionCharacter) {
    return this.getStringWidthDim(text, new Font(), renderSectionCharacter);
  }

  /** Returns the width of this string. Equivalent of FontMetrics.stringWidth(String s). */
  public Dim getStringWidthDim(String text, Font font, boolean renderSectionCharacter) {
    if (text == null) {
      return this.dimFactory.zeroGui();
    } else {
      Dim width = this.dimFactory.zeroGui();
      boolean isBold = false;

      for (int j = 0; j < text.length(); ++j) {
        char thisChar = text.charAt(j);
        Dim charWidth = this.getCharWidth(thisChar, renderSectionCharacter);

        if (thisChar == CHAR_SECTION_SIGN && j < text.length() - 1) {
          if (!renderSectionCharacter) {
            // skip the next character
            j++;
          }

          char formatChar = text.charAt(j); // format char

          if (formatChar != 108 && formatChar != 76) { // is not `l` (bold)
            if (formatChar == 114 || formatChar == 82) { // is `r`
              isBold = false;
            }
          } else {
            isBold = true;
          }

          if (!renderSectionCharacter) {
            charWidth = this.dimFactory.zeroGui();
          }
        }

        width = width.plus(charWidth);

        if ((font.getBold() || isBold) && charWidth.gt(this.dimFactory.zeroGui())) {
          width = width.plus(this.dimFactory.fromGui(1));
        }
      }

      return width;
    }
  }

  /** Returns the width of this character as rendered. */
  public Dim getCharWidth(char character) {
    return this.getCharWidth(character, false);
  }

  /** Returns the width of this character as rendered. */
  public Dim getCharWidth(char character, boolean renderSectionCharacter) {
    if (character == CHAR_SECTION_SIGN && !renderSectionCharacter) {
      return this.dimFactory.fromGui(-1);
    } else if (character == CHAR_SPACE) {
      return this.dimFactory.fromGui(4);
    } else {
      int i = ASCII_CHARACTERS.indexOf(character);

      if (character > 0 && i != -1 && !this.unicodeFlag) {
        return this.dimFactory.fromGui(this.charWidth[i]);
      } else if (this.glyphWidth[character] != 0) {
        int j = this.glyphWidth[character] >>> 4;
        int k = this.glyphWidth[character] & 15;


        ++k;
        return this.dimFactory.fromGui((k - j) * UNICODE_SCALING_FACTOR + 1);
      } else {
        return this.dimFactory.zeroGui();
      }
    }
  }

  /** Trims a string to fit a specified Width. */
  public String trimStringToWidth(String text, int width) {
    return this.trimStringToWidth(text, width, false);
  }

  /** Trims a string to a specified width, and will reverse it if set. */
  public String trimStringToWidth(String text, int width, boolean reverse) {
    return this.trimStringToWidth(text, this.dimFactory.fromGui(width), new Font(), reverse, false);
  }

  /** Trims a string to a specified width, and will reverse it if set. */
  public String trimStringToWidth(String text, int width, boolean reverse, boolean renderSectionCharacter) {
    return this.trimStringToWidth(text, this.dimFactory.fromGui(width), new Font(), reverse, renderSectionCharacter);
  }

  public String trimStringToWidth(String text, Dim width, Font font, boolean reverse) {
    return this.trimStringToWidth(text, width, font, reverse, false);
  }

  /** Trims a string to a specified width, and will reverse it if set. If `renderSectionCharacter` is `true`, the formatting codes are considered in the width calculation. */
  public String trimStringToWidth(String text, Dim width, Font font, boolean reverse, boolean renderSectionCharacter) {
    StringBuilder stringbuilder = new StringBuilder();
    Dim currentWidth = this.dimFactory.zeroGui();
    int startIndex = reverse ? text.length() - 1 : 0;
    int delta = reverse ? -1 : 1;
    boolean isBold = false;

    for (int j = startIndex; j >= 0 && j < text.length() && currentWidth.lt(width); j += delta) {
      char thisChar = text.charAt(j);
      Dim charWidth = this.getCharWidth(thisChar, renderSectionCharacter);

      if (thisChar == CHAR_SECTION_SIGN && j < text.length() - 1) {
        if (!renderSectionCharacter) {
          // skip the next character
          j++;
        }

        char formatChar = text.charAt(j); // format char

        if (formatChar != 108 && formatChar != 76) { // is not `l` (bold)
          if (formatChar == 114 || formatChar == 82) { // is `r`
            isBold = false;
          }
        } else {
          isBold = true;
        }

        if (!renderSectionCharacter) {
          charWidth = this.dimFactory.zeroGui();
        }
      }

      currentWidth = currentWidth.plus(charWidth);

      if ((font.getBold() || isBold) && charWidth.gt(this.dimFactory.zeroGui())) {
        currentWidth = currentWidth.plus(this.dimFactory.fromGui(1));
      }

      if (currentWidth.gt(width)) {
        break;
      }

      if (reverse) {
        stringbuilder.insert(0, thisChar);
      } else {
        stringbuilder.append(thisChar);
      }
    }

    return stringbuilder.toString();
  }

  /** Remove all newline characters from the end of the string */
  private String trimStringNewline(String text) {
    while (text != null && text.endsWith("\n")) {
      text = text.substring(0, text.length() - 1);
    }

    return text;
  }

  /** Splits and draws a String with wordwrap (maximum length is parameter k) */
  public void drawSplitString(String str, Dim x, Dim y, Dim wrapWidth, Font font) {
    str = this.trimStringNewline(str);

    for (String s : this.listFormattedStringToWidth(str, wrapWidth)) {
      this.drawString(s, x, y, font);
      y = y.plus(this.FONT_HEIGHT_DIM);
    }
  }

  /** Returns the width of the wordwrapped String (maximum length is parameter k)
   *
   * @param str The string to split
   * @param maxLength The maximum length of a word */
  public int splitStringWidth(String str, Dim maxLength) {
    return this.FONT_HEIGHT * this.listFormattedStringToWidth(str, maxLength).size();
  }

  /** Set unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
   * font. */
  public void setUnicodeFlag(boolean unicodeFlagIn) {
    this.unicodeFlag = unicodeFlagIn;
  }

  /** Get unicodeFlag controlling whether strings should be rendered with Unicode fonts instead of the default.png
   * font. */
  public boolean getUnicodeFlag() {
    return this.unicodeFlag;
  }

  public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
    return this.listFormattedStringToWidth(str, this.dimFactory.fromGui(wrapWidth));
  }

  public List<String> listFormattedStringToWidth(String str, Dim wrapWidth) {
    return Arrays.<String>asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
  }

  /** Inserts newline and formatting into a string to wrap it within the specified width. */
  String wrapFormattedStringToWidth(String str, Dim wrapWidth) {
    int i = this.sizeStringToWidth(str, wrapWidth);

    if (str.length() <= i) {
      return str;
    } else {
      String s = str.substring(0, i);
      char c0 = str.charAt(i);
      boolean flag = CHAR_SPACE == 32 || c0 == CHAR_NEW_LINE;
      String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
      return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
    }
  }

  /** Determines how many characters from the string will fit into the specified width. */
  private int sizeStringToWidth(String str, Dim wrapWidth) {
    int i = str.length();
    int j = 0;
    int k = 0;
    int l = -1;

    for (boolean flag = false; k < i; ++k) {
      char c0 = str.charAt(k);

      switch (c0) {
        case '\n':
          --k;
          break;
        case ' ':
          l = k;
        default:
          j += (int)this.getCharWidth(c0).getGui();

          if (flag) {
            ++j;
          }

          break;
        case CHAR_SECTION_SIGN:

          if (k < i - 1) {
            ++k;
            char c1 = str.charAt(k);

            if (c1 != 108 && c1 != 76) {
              if (c1 == 114 || c1 == 82 || isFormatColor(c1)) {
                flag = false;
              }
            } else {
              flag = true;
            }
          }
      }

      if (c0 == 10) {
        ++k;
        l = k;
        break;
      }

      if (j > wrapWidth.getGui()) {
        break;
      }
    }

    return k != i && l != -1 && l < k ? l : k;
  }

  /** Checks if the char code is a hexadecimal character, used to set colour. */
  private static boolean isFormatColor(char colorChar) {
    return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
  }

  /** Checks if the char code is O-K...lLrRk-o... used to set special formatting. */
  private static boolean isFormatSpecial(char formatChar) {
    return formatChar >= 107 && formatChar <= 111 || formatChar >= 75 && formatChar <= 79 || formatChar == 114 || formatChar == 82;
  }

  /** Digests a string for nonprinting formatting characters then returns a string containing only that formatting. */
  public static String getFormatFromString(String text) {
    String s = "";
    int i = -1;
    int j = text.length();

    while ((i = text.indexOf(CHAR_SECTION_SIGN, i + 1)) != -1) {
      if (i < j - 1) {
        char c0 = text.charAt(i + 1);

        if (isFormatColor(c0)) {
          s = SECTION_SIGN_STRING + c0;
        } else if (isFormatSpecial(c0)) {
          s = s + SECTION_SIGN_STRING + c0;
        }
      }
    }

    return s;
  }

  protected void bindTexture(ResourceLocation location) {
    this.renderEngine.bindTexture(location);
  }

  protected InputStream getResourceInputStream(ResourceLocation location) throws IOException {
    return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
  }

  public int getColorCode(char character) {
    return this.colorCode["0123456789abcdef".indexOf(character)];
  }

  @FunctionalInterface
  private interface CharRenderer {
    Dim render(char ch, boolean isItalic, Dim x, Dim y, float z);
  }
}
