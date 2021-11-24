package dev.rebel.chatoverlay;

import dev.rebel.chatoverlay.models.chat.ChatItem;
import dev.rebel.chatoverlay.proxy.YtChatProxy;
import dev.rebel.chatoverlay.services.FilterService;
import dev.rebel.chatoverlay.services.McChatService;
import dev.rebel.chatoverlay.services.YtChatListenerService;
import dev.rebel.chatoverlay.services.YtChatService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "chatoverlay", name = "Chat Overlay", version = "1.0")
public class ChatOverlay {
  private final YtChatService ytChatService;
  private final YtChatListenerService ytChatListenerService;
  private final McChatService mcChatService;

  public ChatOverlay() throws Exception {
    String apiPath = "http://localhost:3010/api/";
    YtChatProxy ytChatProxy = new YtChatProxy(apiPath);

    // filtered list copied from https://docs.google.com/document/d/1DiY_JeKsjIxVB42s0fTJ11X1zsP-vL_QutIUkZ722Fk/edit
    // (some generalisation phrases were removed manually)
    String filterPath = "/assets/chatoverlay/filter.txt";
    FilterService filterService = new FilterService('*', filterPath);

    this.ytChatListenerService = new YtChatListenerService();
    this.ytChatService = new YtChatService(ytChatProxy, ytChatListenerService);
    this.mcChatService = new McChatService(filterService);
  }

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) {
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register((Object) new EventHandler());

    ytChatListenerService.listen(this::onNewYtChat);
    ytChatService.start();
  }

  private void onNewYtChat(ChatItem[] newChat) {
    for (ChatItem chat: newChat) {
      this.mcChatService.addToMcChat(chat);
    }
  }
}
