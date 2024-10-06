package dev.rebel.chatmate.gui.builder;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;

import java.util.List;

public abstract class ManagedLayout {
  protected final List<GuiButton> buttonList;
  protected final List<GuiLabel> labelList;

  protected ManagedLayout(List<GuiButton> buttonList, List<GuiLabel> labelList) {
    this.buttonList = buttonList;
    this.labelList = labelList;
  }

  public abstract ManagedLayout instantiate();

  /** Returns true if the event was handled by a button */
  public abstract boolean onActionPerformed(GuiButton button);

  public abstract boolean onPostMousePressed(int posX, int posY);
  public abstract boolean onPostMouseDragged(int posX, int posY);
  public abstract void onPostMouseReleased(int posX, int posY);

  public abstract void refreshContents();
}
