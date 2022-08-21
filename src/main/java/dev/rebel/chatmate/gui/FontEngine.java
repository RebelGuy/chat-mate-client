package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FontEngine {
  private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
  private static final char CHAR_SECTION_SIGN = 167;
  private static final char CHAR_SPACE = 32;
  private static final String SECTION_SIGN_STRING = "\u00A7";
  private static final String STYLE_CODES = "0123456789abcdefklmnor";
  private static final String ASCII_CHARACTERS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
  private static final float UNICODE_SCALING_FACTOR = 0.5f;

  private final DimFactory dimFactory;

  /** Array of width of all the characters in default.png */
  protected int[] charWidth = new int[256];
  /** the height in pixels of default text */
  public int FONT_HEIGHT = 9;
  public Random fontRandom = new Random();
  /** Array of the start/end column (in upper/lower nibble) for every glyph in the /font directory. */
  protected byte[] glyphWidth = new byte[65536];
  /** Array of RGB triplets defining the 16 standard chat colors followed by 16 darker version of the same colors for
   * drop shadows. */
  private final int[] colorCode = new int[32];
  protected final ResourceLocation locationFontTexture;
  /** The RenderEngine used to load and setup glyph textures. */
  private final TextureManager renderEngine;
  /** Current X coordinate at which to draw the next character. */
  protected float posX;
  /** Current Y coordinate at which to draw the next character. */
  protected float posY;
  /** If true, strings should be rendered with Unicode fonts instead of the default.png font */
  private boolean unicodeFlag;

  public FontEngine(DimFactory dimFactory, GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode) {
    this.dimFactory = dimFactory;
    this.locationFontTexture = location;
    this.renderEngine = textureManagerIn;
    this.unicodeFlag = unicode;
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

  /** Render the given char */
  private float renderChar(char ch, Font font) {
    if (ch == CHAR_SPACE) {
      return 4.0F;
    }

    GlStateManager.enableAlpha();
    this.setColor(font.getColour());

    if (font.getSmoothFont()) {
      // required so coloured text shows with the partial transparency
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    int i = ASCII_CHARACTERS.indexOf(ch);
    return i != -1 && !this.unicodeFlag ? this.renderDefaultChar(i, font) : this.renderUnicodeChar(ch, font);
  }

  /** Render a single character with the default.png font at current (posX,posY) location... */
  protected float renderDefaultChar(int ch, Font font) {
    float col = (float)(ch % 16 * 8);
    float row = (float)(ch / 16 * 8);
    float italicTransform = font.getItalic() ? 0.75f : 0;
    bindTexture(this.locationFontTexture);

    // padding helps us avoid abrupt edges of smoothed characters, and also prevents pixels of the neighbouring character in the map from showing up.
    // ensure that the same padding is applied to the texture and vertex numerators, otherwise scaling will occur.
    float paddingRight = 0.8f; // default: 1
    float paddingLeft = -0.2f; // default: 0
    float paddingBottom = 0.2f; // default: 0.1 - characters are abruptly cut off, but reducing this causes the characters in the row below will show up
    float paddingTop = 0.13f; // default: 0 - can't change because sometimes the characters from the row above can show up
    float texCharWidth = this.charWidth[ch];

    // in the future, these are some of the settings that we could add to a Font object, which acts as a preset transform of the font renderer (including colour, perhaps),
    // and which exposes methods for measuring and rendering text.
    // we will be able to set different fonts for different contexts, e.g. the menu font might be different to the chat font, etc, and ChatComponents can specify their own custom font
    float drawnScaleX = 1f; // adjusting this squishes/expands the text in the x direction, drastically changing the look of the font
    float charWidth = texCharWidth * drawnScaleX; // draws them closer together/farther apart

    GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
    GL11.glTexCoord2f((col + paddingLeft) / 128.0F, (row + paddingTop) / 128.0F);
    GL11.glVertex3f(this.posX + paddingLeft + italicTransform, this.posY + paddingTop, 0.0F);
    GL11.glTexCoord2f((col + paddingLeft) / 128.0F, (row + (8 - paddingBottom)) / 128.0F);
    GL11.glVertex3f(this.posX + paddingLeft - italicTransform, this.posY + (8 - paddingBottom), 0.0F);
    GL11.glTexCoord2f((col + texCharWidth - paddingRight) / 128.0F, (row + paddingTop) / 128.0F);
    GL11.glVertex3f(this.posX + texCharWidth * drawnScaleX - paddingRight + italicTransform, this.posY + paddingTop, 0.0F);
    GL11.glTexCoord2f((col + texCharWidth - paddingRight) / 128.0F, (row + (8 - paddingBottom)) / 128.0F);
    GL11.glVertex3f(this.posX + texCharWidth * drawnScaleX - paddingRight - italicTransform, this.posY + (8 - paddingBottom), 0.0F);
    GL11.glEnd();

    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    GlStateManager.disableBlend();

    return charWidth;
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
  protected float renderUnicodeChar(char ch, Font font) {
    if (this.glyphWidth[ch] == 0) {
      return 0.0F;
    } else {
      int page = ch / 256;
      this.loadGlyphTexture(page);
      int j = this.glyphWidth[ch] >>> 4;
      int k = this.glyphWidth[ch] & 15;
      float f = (float)j;
      float f1 = (float)(k + 1);
      float col = (float)(ch % 16 * 16) + f;
      float row = (float)((ch & 255) / 16 * 16);
      float texCharWidth = f1 - f - 0.02F;
      float italicTransform = font.getItalic() ? 1.0F : 0.0F;
      GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
      GL11.glTexCoord2f(col / 256.0F, row / 256.0F);
      GL11.glVertex3f(this.posX + italicTransform, this.posY, 0.0F);
      GL11.glTexCoord2f(col / 256.0F, (row + 15.98F) / 256.0F);
      GL11.glVertex3f(this.posX - italicTransform, this.posY + 7.99F, 0.0F);
      GL11.glTexCoord2f((col + texCharWidth) / 256.0F, row / 256.0F);
      GL11.glVertex3f(this.posX + texCharWidth * UNICODE_SCALING_FACTOR + italicTransform, this.posY, 0.0F);
      GL11.glTexCoord2f((col + texCharWidth) / 256.0F, (row + 15.98F) / 256.0F);
      GL11.glVertex3f(this.posX + texCharWidth * UNICODE_SCALING_FACTOR - italicTransform, this.posY + 7.99F, 0.0F);
      GL11.glEnd();
      return (f1 - f) * UNICODE_SCALING_FACTOR + 1.0F;
    }
  }

  public int drawString(String text, float x, float y, Font font) {
    int i;

    // todo: renderStringAtPos should handle shadow drawing natively, without us having to call this twice. after that change, we don't even need this method anymore and we can rename it to renderString().
    if (font.getShadow() != null) {
      i = this.renderString(text, x + 1.0F, y + 1.0F, font.withColour(font.getShadow().getColour(font))); // shadow render
      return Math.max(i, this.renderString(text, x, y, font)); // normal render, I don't see how its output would be different than the output for the shadow
    } else {
      return this.renderString(text, x, y, font);
    }
  }

  /** Render a single line string at the current (posX,posY) and update posX, respecting the styles contained in the string. */
  private void renderStringAtPos(String text, Font baseFont) {
    Font currentFont = baseFont;

    for (int i = 0; i < text.length(); ++i) {
      char c = text.charAt(i);

      if (c == CHAR_SECTION_SIGN && i + 1 < text.length()) {
        int styleIndex = STYLE_CODES.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
        i++;

        if (styleIndex >= 0 && styleIndex < 16) {
          int colourInt = this.colorCode[styleIndex];
          Colour colour = new Colour(colourInt).withAlpha(baseFont.getColour().alpha);

          // todo: it seems weird that colours must go first, else the stylings are reset, but it seems that this is the vanilla behaviour. double check that it makes sense, otherwise don't reset the stylings.
          currentFont = currentFont
              .withObfuscated(false)
              .withBold(false)
              .withItalic(false)
              .withUnderlined(false)
              .withStrikethrough(false)
              .withColour(colour);
        } else if (styleIndex == 16) {
          currentFont = currentFont.withObfuscated(true);
        } else if (styleIndex == 17) {
          currentFont = currentFont.withBold(true);
        } else if (styleIndex == 18) {
          currentFont = currentFont.withStrikethrough(true);
        } else if (styleIndex == 19) {
          currentFont = currentFont.withUnderlined(true);
        } else if (styleIndex == 20) {
          currentFont = currentFont.withItalic(true);
        } else if (styleIndex == 21) {
          currentFont = baseFont;
        }
      } else {
        int asciiIndex = ASCII_CHARACTERS.indexOf(c);

        // get a random character with the same width
        if (currentFont.getObfuscated() && asciiIndex != -1) {
          int k = this.getCharWidth(c);
          char c1;

          while (true) {
            asciiIndex = this.fontRandom.nextInt(ASCII_CHARACTERS.length());
            c1 = ASCII_CHARACTERS.charAt(asciiIndex);

            if (k == this.getCharWidth(c1)) {
              break;
            }
          }

          c = c1;
        }

        float f1 = asciiIndex == -1 || this.unicodeFlag ? UNICODE_SCALING_FACTOR : 1f;
        boolean isUnicodeAndShadow = (c == 0 || asciiIndex == -1 || this.unicodeFlag) && currentFont.getShadow() != null;

        if (isUnicodeAndShadow) {
          this.posX -= f1;
          this.posY -= f1;
        }

        float charWidth = this.renderChar(c, currentFont);

        if (isUnicodeAndShadow) {
          this.posX += f1;
          this.posY += f1;
        }

        // todo: bold handling and, in fact, all the offsets done here, should be moved to renderChar
        // if bold, offset by 1
        if (currentFont.getBold()) {
          this.posX += f1;

          if (isUnicodeAndShadow) {
            this.posX -= f1;
            this.posY -= f1;
          }

          this.renderChar(c, currentFont);
          this.posX -= f1;

          if (isUnicodeAndShadow) {
            this.posX += f1;
            this.posY += f1;
          }

          ++charWidth;
        }
        drawStylisedArtifacts(charWidth, currentFont);
        this.posX += (float)((int)charWidth); // todo: why is it purposefully rounding down? don't we want to keep the precision?L
      }
    }
  }

  protected void drawStylisedArtifacts(float width, Font font) {
    // todo: needs to respect bold/shadow
    if (font.getStrikethrough()) {
      Tessellator tessellator = Tessellator.getInstance();
      WorldRenderer worldrenderer = tessellator.getWorldRenderer();
      GlStateManager.disableTexture2D();
      worldrenderer.begin(7, DefaultVertexFormats.POSITION);
      worldrenderer.pos(this.posX, (this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
      worldrenderer.pos((this.posX + width), (this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
      worldrenderer.pos((this.posX + width), (this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
      worldrenderer.pos(this.posX, (this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
    }

    if (font.getUnderlined()) {
      Tessellator tessellator1 = Tessellator.getInstance();
      WorldRenderer worldrenderer1 = tessellator1.getWorldRenderer();
      GlStateManager.disableTexture2D();
      worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
      int xCorrection = -1; // no idea why this is applied only on the left, but not on the right
      worldrenderer1.pos((this.posX + (float)xCorrection), (this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
      worldrenderer1.pos((this.posX + width), (this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
      worldrenderer1.pos((this.posX + width), (this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
      worldrenderer1.pos((this.posX + (float)xCorrection), (this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
      tessellator1.draw();
      GlStateManager.enableTexture2D();
    }
  }

  /** Render single line string by setting GL color, current (posX,posY), and calling renderStringAtPos() */
  private int renderString(String text, float x, float y, Font font) {
    if (text == null) {
      return 0;
    } else {
      this.posX = x;
      this.posY = y;
      this.renderStringAtPos(text, font);
      return (int)this.posX;
    }
  }

  /** Returns the width of this string. Equivalent of FontMetrics.stringWidth(String s). */
  public int getStringWidth(String text) {
    if (text == null) {
      return 0;
    } else {
      int i = 0;
      boolean flag = false;

      for (int j = 0; j < text.length(); ++j) {
        char c0 = text.charAt(j);
        int k = this.getCharWidth(c0);

        if (k < 0 && j < text.length() - 1) {
          ++j;
          c0 = text.charAt(j);

          if (c0 != 108 && c0 != 76) {
            if (c0 == 114 || c0 == 82) {
              flag = false;
            }
          } else {
            flag = true;
          }

          k = 0;
        }

        i += k;

        if (flag && k > 0) {
          ++i;
        }
      }

      return i;
    }
  }

  /** Returns the width of this character as rendered. */
  public int getCharWidth(char character) {
    if (character == CHAR_SECTION_SIGN) {
      return -1;
    } else if (character == 32) {
      return 4;
    } else {
      int i = ASCII_CHARACTERS.indexOf(character);

      if (character > 0 && i != -1 && !this.unicodeFlag) {
        return this.charWidth[i];
      } else if (this.glyphWidth[character] != 0) {
        // todo: should we also load the page, like in the `renderUnicodeChar` function?
//        int page = character / 256;
//        this.loadGlyphTexture(page);
        int j = this.glyphWidth[character] >>> 4;
        int k = this.glyphWidth[character] & 15;


        ++k;
        return (int)((k - j) * UNICODE_SCALING_FACTOR) + 1;
      } else {
        return 0;
      }
    }
  }

  /** Trims a string to fit a specified Width. */
  public String trimStringToWidth(String text, int width) {
    return this.trimStringToWidth(text, width, false);
  }

  /** Trims a string to a specified width, and will reverse it if par3 is set. */
  public String trimStringToWidth(String text, int width, boolean reverse) {
    StringBuilder stringbuilder = new StringBuilder();
    int i = 0;
    int j = reverse ? text.length() - 1 : 0;
    int k = reverse ? -1 : 1;
    boolean flag = false;
    boolean flag1 = false;

    for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
      char c0 = text.charAt(l);
      int i1 = this.getCharWidth(c0);

      if (flag) {
        flag = false;

        if (c0 != 108 && c0 != 76) {
          if (c0 == 114 || c0 == 82) {
            flag1 = false;
          }
        } else {
          flag1 = true;
        }
      } else if (i1 < 0) {
        flag = true;
      } else {
        i += i1;

        if (flag1) {
          ++i;
        }
      }

      if (i > width) {
        break;
      }

      if (reverse) {
        stringbuilder.insert(0, c0);
      } else {
        stringbuilder.append(c0);
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
  public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor, Font font) {
    str = this.trimStringNewline(str);

    for (String s : this.listFormattedStringToWidth(str, wrapWidth)) {
      this.renderString(s, x, y, font.withColour(new Colour(textColor)));
      y += this.FONT_HEIGHT;
    }
  }

  /** Returns the width of the wordwrapped String (maximum length is parameter k)
   *
   * @param str The string to split
   * @param maxLength The maximum length of a word */
  public int splitStringWidth(String str, int maxLength) {
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
    return Arrays.<String>asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
  }

  /** Inserts newline and formatting into a string to wrap it within the specified width. */
  String wrapFormattedStringToWidth(String str, int wrapWidth) {
    int i = this.sizeStringToWidth(str, wrapWidth);

    if (str.length() <= i) {
      return str;
    } else {
      String s = str.substring(0, i);
      char c0 = str.charAt(i);
      boolean flag = c0 == 32 || c0 == 10;
      String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
      return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
    }
  }

  /** Determines how many characters from the string will fit into the specified width. */
  private int sizeStringToWidth(String str, int wrapWidth) {
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
          j += this.getCharWidth(c0);

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

      if (j > wrapWidth) {
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

  protected void setColor(Colour colour) {
    GlStateManager.color(colour.redf, colour.greenf, colour.bluef, colour.alphaf);
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
}
