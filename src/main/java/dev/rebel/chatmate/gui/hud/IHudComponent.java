package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.RenderContext;

/** Should be implemented by the component that is drawn to the HUD. All public facing coordinates are GUI coords. */
public interface IHudComponent {
  public float getX();
  public float getY();
  public float getWidth();
  public float getHeight();
  public float getContentScale();

  public boolean canResizeBox();
  public void onResize(float newWidth, float newHeight, boolean keepCentred);

  public boolean canRescaleContent();
  public void onRescaleContent(float newScale);

  public boolean canTranslate();
  public void onTranslate(float newX, float newY);

  public void render(RenderContext context);
}
