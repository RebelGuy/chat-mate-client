package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.models.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;

import java.io.IOException;

public class CustomGuiPause extends GuiIngameMenu {
  private final Config config;
  private GuiButton ytButton;

  // fantastic tutorial here: http://jabelarminecraft.blogspot.com/p/minecraft-modding-configuration-guis.html
  // (use the above when we have config files one day)
  public CustomGuiPause(Config config)
  {
    super();
    this.config = config;
  }

  // great tutorial: https://medium.com/@andreshj87/drawing-a-gui-screen-on-minecraft-forge-7e0059015596
  @Override
  public void initGui()
  {
    super.initGui();

    GuiButton optionsButton = this.buttonList.stream().filter(b -> b.id == 0).findFirst().get();
    GuiButton modOptionsButton = this.buttonList.stream().filter(b -> b.id == 12).findFirst().get();

    int padding = modOptionsButton.xPosition - (optionsButton.xPosition + optionsButton.width);
    int height = optionsButton.height;
    int left = optionsButton.xPosition;
    int right = modOptionsButton.xPosition + modOptionsButton.width;
    int width = right - left;

    // add a square button for "YT"
    int ytWidth = height;
    int ytPosition = right - ytWidth;
    this.ytButton = new GuiButton(727, ytPosition, optionsButton.yPosition, ytWidth, height, this.getButtonText());
    this.buttonList.add(this.ytButton);

    int newWidth = width - ytWidth - padding;
    int newButtonWidths = (newWidth - padding) / 2;
    optionsButton.width = newButtonWidths;
    modOptionsButton.xPosition = left + newButtonWidths + padding;
    modOptionsButton.width = newButtonWidths;

  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException
  {
    if (button == this.ytButton) {
      this.setEnabled(!this.config.apiEnabled.get());
    } else {
      super.actionPerformed(button);
    }
  }

  private void setEnabled(boolean enabled) {
    this.config.apiEnabled.set(enabled);
    this.ytButton.displayString = this.getButtonText();
  }

  private String getButtonText() {
    return this.getButtonText(this.config.apiEnabled.get());
  }

  private String getButtonText(boolean enabled) {
    return (enabled ? "§2" : "§4") + "YT";
  }
}
