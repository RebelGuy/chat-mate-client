package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.ChatMate;
import dev.rebel.chatmate.models.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.ArrayList;

public class CustomGuiConfig extends GuiConfig {
  private final Config config;
  private GuiButton apiButton;
  private GuiButton soundButton;

  // fantastic tutorial here: http://jabelarminecraft.blogspot.com/p/minecraft-modding-configuration-guis.html
  // (use the above when we have config files one day)
  // Note: do NOT change this constructor, as the GuiFactory that instantiates this class is registered with the mod.
  // Todo: In the future, we could add a button-click handler in the CustomGuiModList that does this properly
  public CustomGuiConfig(GuiScreen parent)
  {
    super(parent, new ArrayList<>(), "chatmate", false, false, "Configure ChatMate");
      titleLine2 = "Mod Settings";
      this.config = ChatMate.instance_hack.config;
  }

  // great tutorial: https://medium.com/@andreshj87/drawing-a-gui-screen-on-minecraft-forge-7e0059015596
  @Override
  public void initGui()
  {
    super.initGui();

    // draws a single enabled/disabled button at the top of the screen.
    // it's made redundant by the "YT" toggle in the main menu, but leaving
    // these here for future reference.
    int apiButtonWidth = 100;
    int buttonHeight = 20;
    int top = 40;
    String apiText = this.getButtonText("API", this.config.apiEnabled.get());
    this.apiButton = new GuiButton(0, this.width / 2 - apiButtonWidth / 2, top, apiButtonWidth, buttonHeight, apiText);
    this.buttonList.add(this.apiButton);

    GuiLabel activateLabel = new GuiLabel(fontRendererObj, 1, this.width / 2 - 20, this.height / 2 + 40, 300, 20, 0xFFFFFF);
    this.labelList.add(activateLabel);

    // sound enabled/disabled
    int soundButtonWidth = 100;
    int padding = 10;
    String soundText = this.getButtonText("Sound", this.config.soundEnabled.get());
    this.soundButton = new GuiButton(0, this.width / 2 - soundButtonWidth / 2, top + buttonHeight + padding, soundButtonWidth, buttonHeight, soundText);
    this.buttonList.add(this.soundButton);
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

    if (button == this.apiButton) {
      this.setApiEnabled(!this.config.apiEnabled.get());
    } else if (button == this.soundButton) {
      this.setSoundEnabled(!this.config.soundEnabled.get());
    }
  }

  private void setApiEnabled(boolean enabled) {
    this.config.apiEnabled.set(enabled);
    this.apiButton.displayString = this.getButtonText("API", this.config.apiEnabled.get());
  }

  private void setSoundEnabled(boolean enabled) {
    this.config.soundEnabled.set(enabled);
    this.soundButton.displayString = this.getButtonText("Sound", this.config.soundEnabled.get());
  }

  private String getButtonText(String type, boolean isEnabled) {
    return type + ": " + (isEnabled ? "Enabled" : "Disabled");
  }
}
