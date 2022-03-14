package dev.rebel.chatmate.gui.hud;

import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

public class Colour {
  public static final Colour WHITE = new Colour(Color.WHITE);
  public static final Colour BLACK = new Colour(Color.BLACK);
  public static final Colour RED = new Colour(Color.RED);
  public static final Colour BLUE = new Colour(Color.BLUE);
  public static final Colour CYAN = new Colour(Color.CYAN);
  public static final Colour DKGREY = new Colour(Color.DKGREY);
  public static final Colour GREEN = new Colour(Color.GREEN);
  public static final Colour GREY = new Colour(Color.GREY);
  public static final Colour LTGREY = new Colour(Color.LTGREY);
  public static final Colour ORANGE = new Colour(Color.ORANGE);
  public static final Colour PURPLE = new Colour(Color.PURPLE);
  public static final Colour YELLOW = new Colour(Color.YELLOW);

  public final int red;
  public final int green;
  public final int blue;
  public final int alpha;

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
  }

  public Colour(float red, float green, float blue) {
    this(red, green, blue, 1.0f);
  }

  public Colour(float red, float green, float blue, float alpha) {
    this((int)(red * 255), (int)(green * 255), (int)(blue * 255), (int)(alpha * 255));
  }

  /** Inverse of Colour.toInt() */
  public Colour(int intValue) {
    this(intValue >> 16 & 255, intValue & 255, intValue >> 8 & 255, intValue >> 24 & 255);
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

  private static int toInt(int red, int green, int blue, int alpha) {
    /*
      The font renderer unpacks the color as follows from the 32-bit int:
      this.red = (float)(color >> 16 & 255) / 255.0F;
      this.blue = (float)(color >> 8 & 255) / 255.0F;
      this.green = (float)(color & 255) / 255.0F;
      this.alpha = (float)(color >> 24 & 255) / 255.0F;
     */
    return green | (blue << 8) | (red << 16) | (alpha << 24);
  }
}
