package dev.rebel.chatmate.gui.style;

import dev.rebel.chatmate.gui.models.DimFactory;
import net.minecraft.util.ChatStyle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/** Immutable object containing font stylisation information. */
public class Font {
  private boolean _obfuscated; // k
  private boolean _bold; // l
  private boolean _italic; // o
  private boolean _strikethrough; // m
  private boolean _underlined; // n
  private boolean _smoothFont;
  private @Nonnull Colour _colour;
  private @Nullable Shadow _shadow;

  public Font() {
    this._obfuscated = false;
    this._bold = false;
    this._italic = false;
    this._strikethrough = false;
    this._underlined = false;
    this._smoothFont = false;
    this._colour = Colour.WHITE;
    this._shadow = null;
  }

  public Font(Font parent) {
    this._obfuscated = parent.getObfuscated();
    this._bold = parent.getBold();
    this._italic = parent.getItalic();
    this._strikethrough = parent.getStrikethrough();
    this._underlined = parent.getUnderlined();
    this._smoothFont = parent.getSmoothFont();
    this._colour = parent.getColour();
    this._shadow = parent.getShadow();
  }

  public Font withSmoothFont(boolean smoothFont) {
    return this.update(font -> font._smoothFont = smoothFont);
  }

  public boolean getSmoothFont() {
    return this._smoothFont;
  }

  public Font withObfuscated(boolean obfuscated) {
    return this.update(font -> font._obfuscated = obfuscated);
  }

  public boolean getObfuscated() {
    return this._obfuscated;
  }

  public Font withBold(boolean bold) {
    return this.update(font -> font._bold = bold);
  }

  public boolean getBold() {
    return this._bold;
  }

  public Font withItalic(boolean italic) {
    return this.update(font -> font._italic = italic);
  }

  public boolean getItalic() {
    return this._italic;
  }

  public Font withStrikethrough(boolean strikethrough) {
    return this.update(font -> font._strikethrough = strikethrough);
  }

  public boolean getStrikethrough() {
    return this._strikethrough;
  }

  public Font withUnderlined(boolean underlined) {
    return this.update(font -> font._underlined = underlined);
  }

  public boolean getUnderlined() {
    return this._underlined;
  }

  public Font withColour(@Nonnull Colour colour) {
    return this.update(font -> font._colour = colour);
  }

  public Font withColour(Function<Colour, Colour> colourUpdater) {
    return this.update(font -> font._colour = colourUpdater.apply(font._colour));
  }

  public Colour getColour() {
    return this._colour;
  }

  public Font withShadow(@Nullable Shadow shadow) {
    return this.update(font -> font._shadow = shadow);
  }

  public Font withShadow(@Nullable Function<Font, Shadow> shadowGenerator) {
    return this.withShadow(shadowGenerator == null ? null : shadowGenerator.apply(this));
  }

  public @Nullable Shadow getShadow() {
    return this._shadow;
  }

  private Font update(Consumer<Font> updater) {
    Font newFont = new Font(this);
    updater.accept(newFont);
    return newFont;
  }

  public static Font fromChatStyle(ChatStyle chatStyle, DimFactory dimFactory) {
    return new Font()
        .withColour(Colour.fromChatColour(chatStyle.getColor()))
        .withBold(chatStyle.getBold())
        .withItalic(chatStyle.getItalic())
        .withObfuscated(chatStyle.getObfuscated())
        .withStrikethrough(chatStyle.getStrikethrough())
        .withUnderlined(chatStyle.getUnderlined())
        .withShadow(new Shadow(dimFactory)); // all chat components are rendered to chat with a shadow
  }
}
