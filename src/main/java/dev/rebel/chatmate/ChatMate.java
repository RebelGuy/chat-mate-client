package dev.rebel.chatmate;

import dev.rebel.chatmate.models.chat.ChatItem;
import dev.rebel.chatmate.proxy.YtChatProxy;
import dev.rebel.chatmate.services.*;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;

// refer to mcmod.info for more settings.
@Mod(modid = "chatmate", useMetadata = true, canBeDeactivated = true, guiFactory = "dev.rebel.chatmate.gui.GuiFactory")
public class ChatMate {
  private final YtChatService ytChatService;
  private final YtChatListenerService ytChatListenerService;
  private final McChatService mcChatService;

  // hack until I figure out how to dependency inject into GUI screens
  public static ChatMate instance_hack;

  private boolean _enabled;
  public boolean isEnabled() { return this._enabled; }

  // from https://forums.minecraftforge.net/topic/68571-1122-check-if-environment-is-deobfuscated/
  final boolean isDev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

  public ChatMate() throws Exception {
    instance_hack = this;

    LoggingService loggingService = new LoggingService("log.log", false);

    String apiPath = "http://localhost:3010/api/";
    YtChatProxy ytChatProxy = new YtChatProxy(apiPath);

    // filtered list copied from https://docs.google.com/document/d/1DiY_JeKsjIxVB42s0fTJ11X1zsP-vL_QutIUkZ722Fk/edit
    // (some generalisation phrases were removed manually)
    String filterPath = "/assets/chatmate/filter.txt";
    FilterService filterService = new FilterService('*', filterPath);

    this.ytChatListenerService = new YtChatListenerService();
    this.ytChatService = new YtChatService(ytChatProxy, ytChatListenerService);
    this.mcChatService = new McChatService(loggingService, filterService);
  }

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) {
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register((Object) new EventHandler(this));

    // to make our life easier, auto enable when in a dev environment
    if (this.isDev) {
      this.enable();
    }
  }

  // this doesn't do anything!
  @Mod.EventHandler
  public void onFMLDeactivation(FMLModDisabledEvent event) {
    this.disable();
  }

  public void enable() {
    if (this._enabled) {
      return;
    }

    ytChatListenerService.listen(this::onNewYtChat);
    ytChatService.start();
    this._enabled = true;
  }

  public void disable() {
    if (!this._enabled) {
      return;
    }

    ytChatListenerService.clear();
    ytChatService.stop();
    this._enabled = false;
  }

  private void onNewYtChat(ChatItem[] newChat) {
    for (ChatItem chat: newChat) {
      this.mcChatService.addToMcChat(chat);
    }
  }
}
