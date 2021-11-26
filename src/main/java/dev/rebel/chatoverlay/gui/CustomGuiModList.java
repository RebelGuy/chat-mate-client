package dev.rebel.chatoverlay.gui;

import dev.rebel.chatoverlay.ChatOverlay;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.fml.client.GuiModList;

public class CustomGuiModList extends GuiModList {
  private final ChatOverlay chatOverlay;

  public CustomGuiModList(GuiMainMenu mainMenu, ChatOverlay chatOverlay)
  {
    super(mainMenu);
    this.chatOverlay = chatOverlay;
  }

  @Override
  public void initGui()
  {
    super.initGui();

    // todo: the button is literally useless. in the future we can override the on-click event to implement
    // deactivation for the ChatOverlay mod.
    GuiButton disableModButton = this.buttonList.stream().filter(b -> b.id == 21).findFirst().get();
    this.buttonList.remove(disableModButton);
  }
}
