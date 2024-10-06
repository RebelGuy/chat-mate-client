package dev.rebel.chatmate.gui.style;

import dev.rebel.chatmate.gui.models.DimFactory;
import net.minecraft.util.ChatStyle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

/** Immutable object containing font stylisation information. */
public class Font {
  // we can probably generalise these property-override pairs somehow, but for now this shall do!
  private boolean _obfuscated; // k
  private boolean _overriddenObfuscated;
  private boolean _bold; // l
  private boolean _overriddenBold;
  private boolean _italic; // o
  private boolean _overriddenItalic;
  private boolean _strikethrough; // m
  private boolean _overriddenStrikethrough;
  private boolean _underlined; // n
  private boolean _overriddenUnderlined;
  private boolean _smoothFont;
  private boolean _overriddenSmoothFont;
  private @Nonnull Colour _colour;
  private boolean _overriddenColour;
  private @Nullable Shadow _shadow;
  private boolean _overriddenShadow;

  public Font() {
    this._obfuscated = false;
    this._overriddenObfuscated = false;
    this._bold = false;
    this._overriddenBold = false;
    this._italic = false;
    this._overriddenItalic = false;
    this._strikethrough = false;
    this._overriddenStrikethrough = false;
    this._underlined = false;
    this._overriddenUnderlined = false;
    this._smoothFont = false;
    this._overriddenSmoothFont = false;
    this._colour = Colour.WHITE;
    this._overriddenColour = false;
    this._shadow = null;
    this._overriddenShadow = false;
  }

  public Font(Font parent) {
    this._obfuscated = parent.getObfuscated();
    this._overriddenObfuscated = parent._overriddenObfuscated;
    this._bold = parent.getBold();
    this._overriddenBold = parent._overriddenBold;
    this._italic = parent.getItalic();
    this._overriddenItalic = parent._overriddenItalic;
    this._strikethrough = parent.getStrikethrough();
    this._overriddenStrikethrough = parent._overriddenStrikethrough;
    this._underlined = parent.getUnderlined();
    this._overriddenUnderlined = parent._overriddenUnderlined;
    this._smoothFont = parent.getSmoothFont();
    this._overriddenSmoothFont = parent._overriddenSmoothFont;
    this._colour = parent.getColour();
    this._overriddenColour = parent._overriddenColour;
    this._shadow = parent.getShadow();
    this._overriddenShadow = parent._overriddenShadow;
  }

  public Font withSmoothFont(boolean smoothFont) {
    return this.update(font -> {
      font._smoothFont = smoothFont;
      font._overriddenSmoothFont = true;
    });
  }

  public boolean getSmoothFont() {
    return this._smoothFont;
  }

  public Font withObfuscated(boolean obfuscated) {
    return this.update(font -> {
      font._obfuscated = obfuscated;
      font._overriddenObfuscated = true;
    });
  }

  public boolean getObfuscated() {
    return this._obfuscated;
  }

  public Font withBold(boolean bold) {
    return this.update(font -> {
      font._bold = bold;
      font._overriddenBold = true;
    });
  }

  public boolean getBold() {
    return this._bold;
  }

  public Font withItalic(boolean italic) {
    return this.update(font -> {
      font._italic = italic;
      font._overriddenItalic = true;
    });
  }

  public boolean getItalic() {
    return this._italic;
  }

  public Font withStrikethrough(boolean strikethrough) {
    return this.update(font -> {
      font._strikethrough = strikethrough;
      font._overriddenStrikethrough = true;
    });
  }

  public boolean getStrikethrough() {
    return this._strikethrough;
  }

  public Font withUnderlined(boolean underlined) {
    return this.update(font -> {
      font._underlined = underlined;
      font._overriddenUnderlined = true;
    });
  }

  public boolean getUnderlined() {
    return this._underlined;
  }

  public Font withColour(@Nonnull Colour colour) {
    return this.update(font -> {
      font._colour = colour;
      font._overriddenColour = true;
    });
  }

  public Font withColour(Function<Colour, Colour> colourUpdater) {
    return this.update(font -> {
      font._colour = colourUpdater.apply(font._colour);
      font._overriddenColour = true;
    });
  }

  public Colour getColour() {
    return this._colour;
  }

  public Font withShadow(@Nullable Shadow shadow) {
    return this.update(font -> {
      font._shadow = shadow;
      this._overriddenShadow = true;
    });
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

  /** Applies the given fonts on top of each other, in order. */
  public static Font merge(Font ...fonts) {
    Font result = fonts[0];

    for (int i = 1; i < fonts.length; i++) {
      Font font = fonts[i];
      if (font._overriddenShadow) {
        result = result.withShadow(font.getShadow());
      }
      if (font._overriddenSmoothFont) {
        result = result.withSmoothFont(font.getSmoothFont());
      }
      if (font._overriddenItalic) {
        result = result.withItalic(font.getItalic());
      }
      if (font._overriddenUnderlined) {
        result = result.withUnderlined(font.getUnderlined());
      }
      if (font._overriddenBold) {
        result = result.withBold(font.getBold());
      }
      if (font._overriddenStrikethrough) {
        result = result.withStrikethrough(font.getStrikethrough());
      }
      if (font._overriddenColour) {
        result = result.withColour(font.getColour());
      }
      if (font._overriddenObfuscated) {
        result = result.withObfuscated(font.getObfuscated());
      }
    }

    return result;
  }
}
