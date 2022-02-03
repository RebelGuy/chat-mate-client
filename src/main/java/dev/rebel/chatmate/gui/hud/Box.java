package dev.rebel.chatmate.gui.hud;

/** Note: all private/protected coordinate properties are in SCREEN COORDS, and all public get* coordinates are in GUI COORDS. */
public class Box {
  private int guiScaleMultiplier;
  protected float x;
  protected float y;
  protected float w;
  protected float h;
  private boolean canTranslate;
  private boolean canResize;

  public Box(int guiScaleMultiplier, float x, float y, float w, float h, boolean canTranslate, boolean canResize) {
    this.guiScaleMultiplier = guiScaleMultiplier;
    this.x = this.guiToScreen(x);
    this.y = this.guiToScreen(y);
    this.w = this.guiToScreen(w);
    this.h = this.guiToScreen(h);
    this.canResize = canResize;
    this.canTranslate = canTranslate;
  }

  public float getX() { return screenToGui(this.x); }

  public float getY() { return screenToGui(this.y); }

  public float getWidth() { return screenToGui(this.w); }

  public float getHeight() { return screenToGui(this.h); }

  public boolean canTranslate() { return this.canTranslate; }

  public void onTranslate(float newX, float newY) {
    if (this.canTranslate) {
      this.x = guiToScreen(newX);
      this.y = guiToScreen(newY);
    }
  }

  public boolean canResizeBox() { return this.canResize; }

  public void onResize(float newW, float newH, boolean keepCentred) {
    if (!this.canResize || this.getWidth() == newW && this.getHeight() == newH) {
      return;
    }

    newW = guiToScreen(newW);
    newH = guiToScreen(newH);

    if (keepCentred) {
      this.x -= (newW - this.w) / 2.0f;
      this.y -= (newH - this.h) / 2.0f;
    }

    this.w = newW;
    this.h = newH;
  }

  public float screenToGui(float screen) { return screen / this.guiScaleMultiplier; }

  public float guiToScreen(float screen) { return screen * this.guiScaleMultiplier; }
}
