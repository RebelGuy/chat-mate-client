package dev.rebel.chatoverlay;

import dev.rebel.chatoverlay.models.chat.ChatItem;
import dev.rebel.chatoverlay.proxy.ChatProxy;
import dev.rebel.chatoverlay.services.ChatListenerService;
import dev.rebel.chatoverlay.services.ChatService;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "chatoverlay", name = "Chat Overlay", version = "1.0")
public class ChatOverlay {
  private final ChatService chatService;
  private final ChatListenerService chatListenerService;

  public ChatOverlay() {
    String apiPath = "http://localhost:3010/api/";
    ChatProxy chatProxy = new ChatProxy(apiPath);
    this.chatListenerService = new ChatListenerService();
    this.chatService = new ChatService(chatProxy, chatListenerService);
  }

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) {
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register((Object) new EventHandler());

    chatListenerService.listen(this::onNewYtChat);
    chatService.start();
  }

  private void onNewYtChat(ChatItem[] newChat) {
    for (ChatItem chat: newChat) {
      this.addToMcChat((chat));
    }
  }

  private void addToMcChat(ChatItem item) {
    if (Minecraft.getMinecraft().ingameGUI != null) {
      // todo: colouring/formatting
      // todo: ensure special characters (e.g. normal emojis) don't break this, as well as very long chat (might need to break up string)
      try {
        String raw = String.format("[%s]: %s", item.author.name, item.renderedText);
        ChatComponentText text = new ChatComponentText(raw);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(text);
      } catch (Exception e) {
        // ignore error because it's not critical
        // todo: log error
      }
    }
  }
}
