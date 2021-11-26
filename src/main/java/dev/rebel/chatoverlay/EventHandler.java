package dev.rebel.chatoverlay;

import dev.rebel.chatoverlay.gui.CustomGuiModList;
import dev.rebel.chatoverlay.gui.CustomGuiPause;
import net.minecraft.client.gui.*;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {
  private final ChatOverlay chatOverlay;

  public EventHandler(ChatOverlay chatOverlay) {
    this.chatOverlay = chatOverlay;
  }

  @SideOnly(Side.CLIENT)
  @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
  public void onEvent(GuiOpenEvent event)
  {
    // override some GUIs :)
    if (event.gui instanceof GuiModList) {
      event.gui = new CustomGuiModList(null, this.chatOverlay);
    } else if (event.gui instanceof GuiIngameMenu) {
      event.gui = new CustomGuiPause(this.chatOverlay);
    }
  }
}
