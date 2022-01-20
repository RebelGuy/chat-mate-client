package dev.rebel.chatmate.gui.hud;

/** Should be implemented by the component that is drawn to the HUD */
public interface IHudComponent {
  public int getX();
  public int getY();
  public int getWidth();
  public int getHeight();
  public float getContentScale();

  public boolean canResizeBox();
  public boolean canScaleContents();
  public boolean canTranslate();

  public void transform(int newX, int newY, int newWidth, int newHeight);
  public void setContentScale(float newScale);
}
