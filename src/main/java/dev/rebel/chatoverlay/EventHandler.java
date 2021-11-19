package dev.rebel.chatoverlay;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentText;

public class EventHandler {
  @SubscribeEvent
  public void connect(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
//    ChatComponentText text = new ChatComponentText("Connected");
//     Minecraft.getMinecraft().thePlayer.addChatMessage(text);
//    System.out.println("Connected");
  }

  @SubscribeEvent
  public void loadWorld(WorldEvent.Load event) {
//    ChatComponentText text = new ChatComponentText("World loaded " + event.world.getWorldInfo().getWorldName());
//    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(text);
  }
}