package dev.rebel.chatmate.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Represents a toggle button with [label]: [yes/no] text. */
@SideOnly(Side.CLIENT)
public class GuiListButton extends GuiButton
{
  private boolean checked;
  /** The localization string used by this control. */
  private final String label;
  /** The GuiResponder Object reference. */
  private final GuiPageButtonList.GuiResponder guiResponder;

  public GuiListButton(GuiPageButtonList.GuiResponder responder, int buttonId, int x, int y, String label, boolean checked)
  {
    super(buttonId, x, y, 150, 20, "");
    this.label = label;
    this.checked = checked;
    this.displayString = this.buildDisplayString();
    this.guiResponder = responder;
  }

  /**
   * Builds the localized display string for this GuiListButton
   */
  private String buildDisplayString()
  {
    return I18n.format(this.label, new Object[0]) + ": " + (this.checked ? I18n.format("gui.yes", new Object[0]) : I18n.format("gui.no", new Object[0]));
  }

  public void setChecked(boolean checked)
  {
    this.checked = checked;
    this.displayString = this.buildDisplayString();
    this.guiResponder.onButtonClick(this.id, checked);
  }

  /**
   * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
   * e).
   */
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
  {
    if (super.mousePressed(mc, mouseX, mouseY))
    {
      this.checked = !this.checked;
      this.displayString = this.buildDisplayString();
      this.guiResponder.onButtonClick(this.id, this.checked);
      return true;
    }
    else
    {
      return false;
    }
  }
}
