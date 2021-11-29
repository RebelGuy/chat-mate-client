package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.ChatMate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.fml.client.GuiModList;

public class CustomGuiModList extends GuiModList {
  private final ChatMate chatMate;

  public CustomGuiModList(GuiMainMenu mainMenu, ChatMate chatMate)
  {
    super(mainMenu);
    this.chatMate = chatMate;
  }

  @Override
  public void initGui()
  {
    super.initGui();

    // todo: the button is literally useless. in the future we can override the on-click event to implement
    // deactivation for the ChatMate mod.
    GuiButton disableModButton = this.buttonList.stream().filter(b -> b.id == 21).findFirst().get();
    this.buttonList.remove(disableModButton);
  }
}
