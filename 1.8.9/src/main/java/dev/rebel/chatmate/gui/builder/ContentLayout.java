package dev.rebel.chatmate.gui.builder;

import javax.annotation.Nullable;

/** Layout components with content and an action callback. */
public abstract class ContentLayout<TGui, TActionData> {
  private final String[] width;

  protected ContentLayout(String[] width) {
    this.width = width;
  }

  public String[] getWidth() {
    return this.width;
  }

  public abstract TGui instantiateGui(int x, int y, int width, int height);
  public abstract @Nullable TGui tryGetGui();
  public abstract void refreshContents();
  public abstract boolean dispatchAction(TActionData actionData);

  public static class NO_ACTION { }
}