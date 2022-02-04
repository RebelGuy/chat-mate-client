package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

import javax.annotation.Nullable;
import java.util.Date;

public class LiveViewersComponent extends Box implements IHudComponent {
  private final static int COLOR = 0xFFFFFFFF;
  private final static int ANIMATION_DURATION = 1500;
  private final static float PI = (float)Math.PI;

  private final StatusService statusService;
  private final Config config;
  private final float initialScale;
  private float scale;
  private final Minecraft minecraft;
  private final Cyclic cyclic = new Cyclic(0, PI * 2);

  private float[] startRotation = new float[2];
  private float[] targetRotation = new float[2];
  private float currentFrac = 0;
  private long prevUpdate = 0;

  public LiveViewersComponent(int guiScaleMultiplier, float initialScale, StatusService statusService, Config config, Minecraft minecraft) {
    // giving values in Gui coords. so at 2x gui scale, with an initial scale of 0.5f, will be at screen coords (20, 20) with a screen size of 16x16
    super(guiScaleMultiplier, 23, 15, 0, 0, true, true);
    this.statusService = statusService;
    this.config = config;
    this.initialScale = initialScale;
    this.scale = initialScale;
    this.minecraft = minecraft;

    this.config.getShowLiveViewers().listen(this::onShowLiveViewers);
  }

  @Override
  public float getContentScale() { return this.scale; }

  @Override
  public boolean canRescaleContent() { return true; }

  @Override
  public void onRescaleContent(float newScale) {
    newScale = Math.max(0.2f, newScale);
    newScale = Math.min(newScale, 5.0f);
    if (this.scale == newScale) {
      return;
    }

    this.onResize(this.getTextWidth() * newScale, this.getTextHeight() * newScale, Anchor.LEFT_CENTRE);
    this.scale = newScale;
  }

  @Override
  public void render(RenderContext context) {
    if (!this.config.getShowLiveViewers().get() || this.getFontRenderer() == null) {
      return;
    }

    // we have to update this constantly because of dynamic content
    this.onResize(this.getTextWidth() * this.scale, this.getTextHeight() * this.scale, Anchor.LEFT_CENTRE);

    GlStateManager.pushMatrix();
    GlStateManager.translate(this.getX(), this.getY(), 0);
    GlStateManager.scale(this.scale, this.scale, 1);

    String text = this.getText();
    if (this.statusService.getLiveViewerCount() == null || text.length() != this.targetRotation.length) {
      this.getFontRenderer().drawStringWithShadow(text, 0, 0, COLOR);
    } else {
      int digitWidth = this.getFontRenderer().getCharWidth('0');
      int digitPadding = 1;
      float digitHeight = this.getTextHeight();
      this.drawReel(text, digitWidth, digitPadding, digitHeight);
    }

    GlStateManager.popMatrix();
  }

  private void drawReel(String text, int digitWidth, int digitPadding, float digitHeight) {
    // get target rotations
    char[] chars = text.toCharArray();
    float[] newTarget = new float[this.targetRotation.length];
    for (int i = 0; i < chars.length; i++) {
      newTarget[i] = this.charToRotation(chars[i]);
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
    for (int i = 0; i < this.targetRotation.length; i++) {
      current[i] = this.getRotation(this.startRotation[i], this.targetRotation[i], this.currentFrac);
    }

    // render
    for (int i = 0; i < current.length; i++) {
      this.drawReelDigit(i * (digitWidth + digitPadding), current[i], digitHeight);
    }

    this.prevUpdate = new Date().getTime();
  }

  private float getRotation(float start, float target, float frac) {
    // the easing function is a trig function, where the y-axis denotes progress (from 0 to 1).
    float progress = (float)(0.5f - 0.5f * Math.cos(PI * frac));
    float distance = this.cyclic.distance(start, target);
    return this.cyclic.add(start, progress * distance);
  }

  private void advanceFrac() {
    long now = new Date().getTime();
    float deltaFrac = (float)(now - this.prevUpdate) / ANIMATION_DURATION;
    this.currentFrac = Math.min(1, this.currentFrac + deltaFrac);
  }

  private float charToRotation(char c) {
    if (!Character.isDigit(c)) {
      return 0;
    } else {
      float normalisedRotation = Character.getNumericValue(c) / 10.0f;
      return this.cyclic.map(normalisedRotation);
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

  private void drawReelDigit(int x, float rotation, float digitHeight) {
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

      // linear
      // for some reason there is padding at the top of text, so the offset to the bottom appears to be much less.
      // to circumvent this, offset the bottom more
      float offsetMultiplier = strength > 0 ? 1.33f : 0.67f;
      float yOffset = offsetMultiplier * strength * digitHeight;

      // inverse parabola with value of 0.2 at extremities
      float scale = -(strength * strength) * 0.8f + 1;

      this.drawDigit(x, yOffset, digit, scale, alpha);
    }
  }

  private void drawDigit(float x, float y, int digit, float scale, float alpha) {
    String text = String.valueOf(digit);
    float width = this.getFontRenderer().getStringWidth(text);

    GlStateManager.pushMatrix();

    x = x + width * (1 - scale) / 2; // keep centred on its x position
    GlStateManager.translate(x, y, 10);
    GlStateManager.scale(scale, scale, scale);

    Colour color = new Colour(1, 1, 1, alpha);
    this.getFontRenderer().drawStringWithShadow(text, 0, 0, color.toSafeInt());

    GlStateManager.popMatrix();
  }

  private void onShowLiveViewers(boolean enabled) {
    float x = 23;
    float y = 15;
    float w = enabled ? this.getTextWidth() * this.scale : 0;
    float h = enabled ? this.getTextHeight() * this.scale : 0;
    this.setRect(x, y, w, h);
    this.scale = this.initialScale;
    this.startRotation = new float[2];
    this.targetRotation = new float[2];
    this.currentFrac = 0;
  }

  private float getTextWidth() {
    if (this.getFontRenderer() == null) {
      return 0;
    } else {
      String text = this.getText();
      return this.getFontRenderer().getStringWidth(text);
    }
  }

  private float getTextHeight() {
    if (this.getFontRenderer() == null) {
      return 0;
    } else {
      return this.getFontRenderer().FONT_HEIGHT;
    }
  }

  private String getText() {
    @Nullable Integer count = this.statusService.getLiveViewerCount();
    if (count == null) {
      return "n/a";
    } else {
      return String.format("%02d", count);
    }
  }

  private @Nullable FontRenderer getFontRenderer() {
    return this.minecraft.fontRendererObj;
  }
}
