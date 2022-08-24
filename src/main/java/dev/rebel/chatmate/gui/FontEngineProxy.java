package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.Colour;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.style.Shadow;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FontEngineProxy extends FontRenderer {
  private final FontEngine fontEngine;
  private final DimFactory dimFactory;

  public FontEngineProxy(FontEngine fontEngine, DimFactory dimFactory, GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode) {
    super(gameSettingsIn, location, textureManagerIn, unicode);

    this.fontEngine = fontEngine;
    this.dimFactory = dimFactory;
  }

  @Override
  public final void onResourceManagerReload(IResourceManager resourceManager) {
    this.fontEngine.onResourceManagerReload(resourceManager);
  }

  @Override
  protected final float renderDefaultChar(int ch, boolean italic) {
    return 0;
  }

  @Override
  protected final float renderUnicodeChar(char ch, boolean italic) {
    return 0;
  }

  @Override
  public final int drawStringWithShadow(String text, float x, float y, int color) {
    return this.drawString(text, x, y, color, true);
  }

  @Override
  public final int drawString(String text, int x, int y, int color) {
    return this.drawString(text, (float)x, (float)y, color, false);
  }

  @Override
  public final int drawString(String text, float x, float y, int color, boolean dropShadow) {
    return (int)this.fontEngine.drawString(text, this.dimFactory.fromGui(x), this.dimFactory.fromGui(y), new Font().withColour(new Colour(color)).withShadow(dropShadow ? new Shadow(this.dimFactory) : null)).getGui();
  }

  @Override
  protected final void doDraw(float f) {
    // only called by the underlying FontRenderer
  }

  @Override
  public final int getStringWidth(String text) {
    return (int)this.fontEngine.getStringWidthDim(text).getGui();
  }

  @Override
  public final int getCharWidth(char character) {
    return this.fontEngine.getCharWidth(character);
  }

  @Override
  public final String trimStringToWidth(String text, int width) {
    return this.fontEngine.trimStringToWidth(text, width);
  }

  @Override
  public final String trimStringToWidth(String text, int width, boolean reverse) {
    return this.fontEngine.trimStringToWidth(text, width, reverse);
  }

  @Override
  public final void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
    this.fontEngine.drawSplitString(str, this.dimFactory.fromGui(x), this.dimFactory.fromGui(y), this.dimFactory.fromGui(wrapWidth), new Font().withColour(new Colour(textColor)));
  }

  @Override
  public final int splitStringWidth(String str, int maxLength) {
    return this.fontEngine.splitStringWidth(str, this.dimFactory.fromGui(maxLength));
  }

  @Override
  public final void setUnicodeFlag(boolean unicodeFlagIn) {
    this.fontEngine.setUnicodeFlag(unicodeFlagIn);
  }

  @Override
  public final boolean getUnicodeFlag() {
    return this.fontEngine.getUnicodeFlag();
  }

  @Override
  public final void setBidiFlag(boolean bidiFlagIn) {
    // bi-directional text is not supported
  }

  @Override
  public final List<String> listFormattedStringToWidth(String str, int wrapWidth) {
    return this.fontEngine.listFormattedStringToWidth(str, this.dimFactory.fromGui(wrapWidth));
  }

  // can't use @Override here, wtf?!
  public final String wrapFormattedStringToWidth(String str, int wrapWidth) {
    return this.fontEngine.wrapFormattedStringToWidth(str, this.dimFactory.fromGui(wrapWidth));
  }

  @Override
  public final boolean getBidiFlag() {
    // bi-directional text is not supported
    return false;
  }

  @Override
  protected final void setColor(float r, float g, float b, float a) {
    // internal method
  }

  @Override
  protected final void enableAlpha() {
    // automatically enabled during rendering
  }

  @Override
  protected final void bindTexture(ResourceLocation location) {
    // this gets called in the constructor of the base class, before we get a chance to set the font engine
    if (this.fontEngine == null) {
      return;
    }
    this.fontEngine.bindTexture(location);
  }

  @Override
  protected final InputStream getResourceInputStream(ResourceLocation location) throws IOException {
    if (this.fontEngine == null) {
      return new InputStream() {
        @Override
        public int read() throws IOException {
          return 0;
        }
      };
    }

    return this.fontEngine.getResourceInputStream(location);
  }

  @Override
  public final int getColorCode(char character) {
    return this.fontEngine.getColorCode(character);
  }
}
