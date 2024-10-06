package dev.rebel.chatmate.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraftforge.client.GuiIngameForge;

/** This class is only here to override the public interface of the GuiIngame object. It doesn't do any work. */
public class CustomGuiIngame extends GuiIngameForge {
  private final GuiNewChat guiNewChat;

  public CustomGuiIngame(Minecraft minecraft, CustomGuiNewChat customGuiNewChat) {
    super(minecraft);
    this.guiNewChat = customGuiNewChat;
  }

  @Override
  public GuiNewChat getChatGUI() {
    return this.guiNewChat;
  }
}
