package dev.rebel.chatmate.gui.Interactive.Events;

import dev.rebel.chatmate.gui.models.DimPoint;

public class ScreenSizeData {
  public final DimPoint oldSize;
  public final int oldScaleFactor;
  public final DimPoint newSize;
  public final int newScaleFactor;

  public ScreenSizeData(DimPoint oldSize, int oldScaleFactor, DimPoint newSize, int newScaleFactor) {
    this.oldSize = oldSize;
    this.oldScaleFactor = oldScaleFactor;
    this.newSize = newSize;
    this.newScaleFactor = newScaleFactor;
  }
}
