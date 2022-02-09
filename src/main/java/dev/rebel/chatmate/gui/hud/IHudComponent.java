package dev.rebel.chatmate.gui.hud;

import dev.rebel.chatmate.gui.RenderContext;
import dev.rebel.chatmate.gui.models.Dim;

/** Should be implemented by the component that is drawn to the HUD. All public facing coordinates are GUI coords. */
public interface IHudComponent {
  public Dim getX();
  public Dim getY();
  public Dim getWidth();
  public Dim getHeight();
  public float getContentScale();

  public boolean canResizeBox();
  public void onResize(Dim newWidth, Dim newHeight, Anchor resizeAnchor);

  public boolean canRescaleContent();
  public void onRescaleContent(float newScale);

  public boolean canTranslate();
  public void onTranslate(Dim newX, Dim newY);

  public void render(RenderContext context);

  /** Denotes the point of the box. */
  public enum Anchor {
    TOP_LEFT,
    TOP_CENTRE,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTRE,
    BOTTOM_RIGHT,
    LEFT_CENTRE,
    RIGHT_CENTRE,
    MIDDLE
  }
}
