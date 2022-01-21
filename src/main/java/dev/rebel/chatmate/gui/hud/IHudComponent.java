package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.RenderContext;

/** Should be implemented by the component that is drawn to the HUD */
public interface IHudComponent {
  public int getX();
  public int getY();
  public int getWidth();
  public int getHeight();
  public float getContentScale();

  public boolean canResizeBox();
  public void onResize(int newWidth, int newHeight);

  public boolean canRescaleContent();
  public void onRescaleContent(float newScale);

  public boolean canTranslate();
  public void onTranslate(int newX, int newY);

  public void render(RenderContext context);
}
