package dev.rebel.chatmate.gui.hud;

import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

public class Colour {
  public static final Colour WHITE = new Colour(Color.WHITE);
  public static final Colour BLACK = new Colour(Color.BLACK);
  public static final Colour RED = new Colour(Color.RED);
  public static final Colour DARK_RED = new Colour(170, 0, 0); // same as Minecraft's DARK_RED style
  public static final Colour LIGHT_RED = new Colour(255, 85, 85); // same as Minecraft's RED style
  public static final Colour BLUE = new Colour(Color.BLUE);
  public static final Colour CYAN = new Colour(Color.CYAN);
  public static final Colour DKGREY = new Colour(Color.DKGREY);
  public static final Colour GREEN = new Colour(Color.GREEN);
  public static final Colour GREY = new Colour(Color.GREY);
  public static final Colour LTGREY = new Colour(Color.LTGREY);
  public static final Colour ORANGE = new Colour(Color.ORANGE);
  public static final Colour PURPLE = new Colour(Color.PURPLE);
  public static final Colour DARK_PURPLE = new Colour(170, 0, 170); // same as Minecraft's DARK_PURPLE style
  public static final Colour YELLOW = new Colour(Color.YELLOW);
  public static final Colour TRANSPARENT = new Colour(Color.WHITE).withAlpha(0);
  public static final Colour BEIGE = new Colour(245, 245, 220);
  public static final Colour ACTION_HOVER = new Colour(16777120); // the default text colour when hovering

  public final int red;
  public final int green;
  public final int blue;
  public final int alpha;
  public final float redf;
  public final float greenf;
  public final float bluef;
  public final float alphaf;

  public Colour(int red, int green, int blue) {
    this(red, green, blue, 255);
  }

  public Colour(Color colour) {
    this(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha());
  }

  public Colour(ReadableColor colour) {
    this(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha());
  }

  public Colour(int red, int green, int blue, int alpha) {
    if (red < 0 || red > 255) {
      throw new RuntimeException("Red is out of bounds: " + red);
    }
    if (green < 0 || green > 255) {
      throw new RuntimeException("Green is out of bounds: " + green);
    }
    if (blue < 0 || blue > 255) {
      throw new RuntimeException("Blue is out of bounds: " + blue);
    }
    if (alpha < 0 || alpha > 255) {
      throw new RuntimeException("Alpha is out of bounds: " + alpha);
    }

    this.red = red;
    this.green = green;
    this.blue = blue;
    this.alpha = alpha;

    this.redf = red / 255.0f;
    this.greenf = green / 255.0f;
    this.bluef = blue / 255.0f;
    this.alphaf = alpha / 255.0f;
  }

  public Colour(float red, float green, float blue) {
    this(red, green, blue, 1.0f);
  }

  public Colour(float red, float green, float blue, float alpha) {
    this((int)(red * 255), (int)(green * 255), (int)(blue * 255), (int)(alpha * 255));
  }

  /** Inverse of Colour.toInt(). Note that if the alpha component is 0, it will be automatically set to 100% as that is most likely the intention. */
  public Colour(int intValue) {
    this(intValue >> 16 & 255, intValue >> 8 & 255, intValue & 255, ((intValue >> 24 & 255) == 0) ? 255 : intValue >> 24 & 255);
  }

  public int toInt() {
    return toInt(this.red, this.green, this.blue, this.alpha);
  }

  /* Fixes low alpha values so Minecraft can render it. */
  public int toSafeInt() {
    int alpha = this.alpha;
    if (alpha < 4) {
      // for some reason it doesn't like extremely low alphas and renders them at 100% alpha
      alpha = 4;
    }

    return toInt(this.red, this.green, this.blue, alpha);
  }

  public Colour withAlpha(int alpha) {
    return new Colour(this.red, this.green, this.blue, alpha);
  }

  public Colour withAlpha(float alpha) {
    int alphaInt = (int)(alpha * 255);
    return this.withAlpha(alphaInt);
  }

  /** Scales all colours by the given value. */
  public Colour withBrightness(float brightness) {
    return new Colour((int)(this.red * brightness), (int)(this.green * brightness), (int)(this.blue * brightness), this.alpha);
  }

  public static Colour lerp(Colour from, Colour to, float frac) {
    return new Colour(
        lerpInt(from.red, to.red, frac),
        lerpInt(from.green, to.green, frac),
        lerpInt(from.blue, to.blue, frac),
        lerpInt(from.alpha, to.alpha, frac)
    );
  }

  private static int toInt(int red, int green, int blue, int alpha) {
    /*
      The font renderer unpacks the color as follows from the 32-bit int:
      this.red = (float)(color >> 16 & 255) / 255.0F;
      this.blue = (float)(color >> 8 & 255) / 255.0F;
      this.green = (float)(color & 255) / 255.0F;
      this.alpha = (float)(color >> 24 & 255) / 255.0F;

      It then passes it to the GlStateManager::color in that order, however, that method expect the order to be RGBA.
      So providing Colour.GREEN to the fontRenderer will be interpreted as green by the renderer, but passed as blue to the state manager.
      Therefore, we have to swap blue and green colours when converting dealing with int conversions.
     */
    return blue | (green << 8) | (red << 16) | (alpha << 24);
  }

  private static int lerpInt(int from, int to, float frac) {
    return (int)(from + (to - from) * frac);
  }
}
