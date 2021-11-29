package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.ChatMate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.ArrayList;

public class CustomGuiConfig extends GuiConfig {
  private final ChatMate modInstance;
  private GuiButton activateButton;
  private GuiLabel activateLabel;

  // fantastic tutorial here: http://jabelarminecraft.blogspot.com/p/minecraft-modding-configuration-guis.html
  // (use the above when we have config files one day)
  // Note: do NOT change this constructor, as the GuiFactory that instantiates this class is registered with the mod.
  // Todo: In the future, we could add a button-click handler in the CustomGuiModList that does this properly
  public CustomGuiConfig(GuiScreen parent)
  {
    super(parent, new ArrayList<>(), "chatmate", false, false, "Configure ChatMate");
      titleLine2 = "Mod Settings";
      this.modInstance = ChatMate.instance_hack;
  }

  // great tutorial: https://medium.com/@andreshj87/drawing-a-gui-screen-on-minecraft-forge-7e0059015596
  @Override
  public void initGui()
  {
    super.initGui();

    // draws a single enabled/disabled button at the top of the screen.
    // it's made redundant by the "YT" toggle in the main menu, but leaving
    // these here for future reference.
    int buttonWidth = 100;
    int buttonHeight = 20;
    this.activateButton = new GuiButton(0, this.width / 2 - buttonWidth / 2, 40, buttonWidth, buttonHeight, this.getButtonText());
    this.buttonList.add(this.activateButton);

    this.activateLabel = new GuiLabel(fontRendererObj, 1, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF);
    this.labelList.add(this.activateLabel);
  }


  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks)
  {
    // You can do things like create animations, draw additional elements, etc. here
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void actionPerformed(GuiButton button)
  {
    super.actionPerformed(button);

    if (button == this.activateButton) {
      this.setEnabled(!this.modInstance.isEnabled());
    }
  }

  private void setEnabled(boolean enabled) {
    if (enabled) {
      this.modInstance.enable();
    } else {
      this.modInstance.disable();
    }

    this.activateButton.displayString = this.getButtonText();
  }

  private String getButtonText() {
    return this.getButtonText(modInstance.isEnabled());
  }

  private String getButtonText(boolean enabled) {
    return enabled ? "Enabled" : "Disabled";
  }
}
