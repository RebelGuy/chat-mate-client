package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.style.Font;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.style.Shadow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Date;

/** Supports two digits at the moment. */
public class DigitReel {
  private final static int ANIMATION_DURATION = 1500;
  private final static float PI = (float)Math.PI;
  private final Cyclic cyclic = new Cyclic(0, PI * 2);
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final FontEngine fontEngine;

  private final Font font;

  // all floats are in radians.
  // e.g. to represent the number 10, the tens-reel moves by 0.1*(2*pi) radians, while the ones wheel moves by 1*(2*pi) radians.
  private float[] startRotation = new float[2];
  private float[] targetRotation = new float[2];
  private float currentFrac = 0;
  private long prevUpdate = 0;

  public DigitReel(Minecraft minecraft, DimFactory dimFactory, FontEngine fontEngine) {
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;
    this.fontEngine = fontEngine;

    this.font = new Font().withShadow(new Shadow(dimFactory));
  }

  public void drawReel(String text, Dim digitWidth, Dim digitPadding, Dim digitHeight) {
    // get target rotations
    char[] chars = text.toCharArray();
    float[] newTarget = new float[this.targetRotation.length];
    for (int i = 0; i < chars.length; i++) {
      // the base will be added to the normalised rotation for each digit, and it corresponds to the
      // tens/hundeds, etc base value applied by the previous digit(s), i.e. how does the rotation compare to the
      // rest of the reel
      int base = 0;
      for (int j = 0; j < i; j++) {
        // newTarget[j] always exists already, and may have also applied a 10 multiplier so it is increasing the further right we go
        base += 10 * newTarget[j] / 2 / PI;
      }
      newTarget[i] = this.charToRotation(chars[i], base);
    }

    // check if the target has updated
    if (!compareArrays(newTarget, this.targetRotation)) {
      this.currentFrac = 0;
      this.startRotation = this.targetRotation.clone();
      this.targetRotation = newTarget;
    }

    // iterate current rotations to the next step
    this.advanceFrac();
    float[] current = new float[this.targetRotation.length];
    float[] speed = new float[this.targetRotation.length];
    for (int i = 0; i < this.targetRotation.length; i++) {
      current[i] = this.getRotation(this.startRotation[i], this.targetRotation[i], this.currentFrac);
      speed[i] = this.getSpeed(this.startRotation[i], this.targetRotation[i], this.currentFrac);
    }

    // render
    for (int i = 0; i < current.length; i++) {
      this.drawReelDigit(digitWidth.plus(digitPadding).times(i), current[i], speed[i], digitHeight);
    }

    this.prevUpdate = new Date().getTime();
  }

  private float getRotation(float start, float target, float frac) {
    // the easing function is a trig function, where the y-axis denotes progress (from 0 to 1).
    float progress = (float)(0.5f - 0.5f * Math.cos(PI * frac));
    float distance = target - start;
    return start + progress * distance;
  }

  private float getSpeed(float start, float target, float frac) {
    float relSpeed = (float)(PI / 2 * Math.sin(PI * frac));
    float distance = target - start;
    return relSpeed * Math.abs(distance);
  }

  private void advanceFrac() {
    long now = new Date().getTime();
    float deltaFrac = (float)(now - this.prevUpdate) / ANIMATION_DURATION;
    this.currentFrac = Math.min(1, this.currentFrac + deltaFrac);
  }

  private float charToRotation(char c, int base) {
    if (!Character.isDigit(c)) {
      return 0;
    } else {
      // originally, we didn't multiply the digit positions by the relevant factor of 10
      // this looked odd, because digits were moving independently when in reality
      // they should be moving coherently as part of a single number.
      float normalisedRotation = Character.getNumericValue(c) / 10.0f;
      return (normalisedRotation + base) * 2 * PI;
    }
  }

  private static boolean compareArrays(float[] a, float[] b) {
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) {
        return false;
      }
    }
    return true;
  }

  private void drawReelDigit(Dim x, float rotation, float speed, Dim digitHeight) {
    float increment = 2 * PI / 10;
    float eps = 0.01f;

    for (int digit = 0; digit < 10; digit++) {
      float digitRotation = digit * increment;
      float distance = this.cyclic.distance(digitRotation, rotation);
      if (Math.abs(distance) >= increment - eps) {
        // digit is not visible at all
        continue;
      }

      // from -1 to 1, where 0 means we are "looking right at the digit"
      float strength = distance / (increment - eps);

      // linear
      float alpha = 1 - Math.abs(strength);

      // linear - this determines how closely the digits are spaced vertically
      // for some reason there is padding at the top of text, so the offset to the bottom appears to be much less.
      // to circumvent this, offset the bottom more
      float offsetMultiplier = strength > 0 ? 1 : 0.5f;
      Dim yOffset = digitHeight.times(offsetMultiplier * strength);

      // inverse parabola with value of 0.2 at extremities
      float scale = -(strength * strength) * 0.8f + 1;

      // show motion blur if fast enough
      boolean highSpeed = false;
      float maxSpeed = PI * PI / 2; // this used to be the max speed when digits moved independently, but is now simply an imperial measure that "looks nice"
      float threshold = maxSpeed / 2;
      if (speed > threshold) {
        float alphaMultiplier = 0.5f;
        float blurOffsetMultiplier = 1;
        float blur = Math.min(2, (speed - threshold) / threshold); // between 0 and 2 units
        float blurredScale = Math.max(0.2f, (1 - 0.2f * blur) * scale); // up to 80% narrower

        if (speed > threshold / 2) {
          highSpeed = true;

          // far, very faded
          Dim yOffsetDeltaFar = this.dimFactory.fromGui(blur * (2 * blurOffsetMultiplier));
          this.drawDigit(x, yOffset.plus(yOffsetDeltaFar), digit, blurredScale, scale, alpha * (alphaMultiplier / 2));
          this.drawDigit(x, yOffset.minus(yOffsetDeltaFar), digit, blurredScale, scale, alpha * (alphaMultiplier / 2));

          // close, slightly faded
          Dim yOffsetDeltaClose = this.dimFactory.fromGui(blur * blurOffsetMultiplier / 2);
          this.drawDigit(x, yOffset.plus(yOffsetDeltaClose), digit, blurredScale, scale, alpha * alphaMultiplier);
          this.drawDigit(x, yOffset.minus(yOffsetDeltaClose), digit, blurredScale, scale, alpha * alphaMultiplier);

        } else {
          // medium closeness, slightly faded
          Dim yOffsetDelta = this.dimFactory.fromGui(blur * blurOffsetMultiplier);
          this.drawDigit(x, yOffset.plus(yOffsetDelta), digit, scale, blurredScale, alpha * alphaMultiplier);
          this.drawDigit(x, yOffset.minus(yOffsetDelta), digit, scale, blurredScale, alpha * alphaMultiplier);
        }
      }

      if (!highSpeed) {
        // well-defined at low speeds
        this.drawDigit(x, yOffset, digit, scale, scale, alpha);
      }
    }
  }

  private void drawDigit(Dim x, Dim y, int digit, float scaleX, float scaleY, float alpha) {
    String text = String.valueOf(digit);
    float wGui = this.fontEngine.getStringWidth(text);
    float xGui = x.getGui();
    float yGui = y.getGui();

    GlStateManager.pushMatrix();

    xGui = xGui + wGui * (1 - scaleX) / 2; // keep centred on its x position
    GlStateManager.translate(xGui, yGui, 10);
    GlStateManager.scale(scaleX, scaleY, 1);

    this.fontEngine.drawString(text, 0, 0, this.font.withColour(c -> c.withAlpha(alpha)));

    GlStateManager.popMatrix();
  }

}
