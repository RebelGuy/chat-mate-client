package dev.rebel.chatmate.gui.hud;

public class TextComponent implements IHudComponent {
  @Override
  public int getX() {
    return 0;
  }

  @Override
  public int getY() {
    return 0;
  }

  @Override
  public int getWidth() {
    return 0;
  }

  @Override
  public int getHeight() {
    return 0;
  }

  @Override
  public float getContentScale() {
    return 0;
  }

  @Override
  public boolean canResizeBox() {
    return false;
  }

  @Override
  public boolean canScaleContents() {
    return false;
  }

  @Override
  public boolean canTranslate() {
    return false;
  }

  @Override
  public void transform(int newX, int newY, int newWidth, int newHeight) {

  }

  @Override
  public void setContentScale(float newScale) {

  }
}
