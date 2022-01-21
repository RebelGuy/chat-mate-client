package dev.rebel.chatmate.gui.hud;

public class Box {
  public int x;
  public int y;
  public int w;
  public int h;
  public boolean canTranslate;
  public boolean canResize;

  public Box(int x, int y, int w, int h, boolean canTranslate, boolean canResize) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.canResize = canResize;
    this.canTranslate = canTranslate;
  }

  public int getX() { return this.x; }

  public int getY() { return this.y; }

  public int getWidth() { return this.w; }

  public int getHeight() { return this.h; }

  public boolean canTranslate() { return this.canTranslate; }

  public void onTranslate(int newX, int newY) {
    if (this.canTranslate) {
      this.x = newX;
      this.y = newY;
    }
  }

  public boolean canResizeBox() { return this.canResize; }

  public void onResize(int newW, int newH) {
    if (this.canResize) {
      this.w = newW;
      this.h = newH;
    }
  }
}
