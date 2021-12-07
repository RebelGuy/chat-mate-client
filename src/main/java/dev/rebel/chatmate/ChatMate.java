package dev.rebel.chatmate;

import dev.rebel.chatmate.models.chat.ChatItem;
import dev.rebel.chatmate.proxy.YtChatProxy;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.util.TextUtilityService;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;

// refer to mcmod.info for more settings.
@Mod(modid = "chatmate", useMetadata = true, canBeDeactivated = true, guiFactory = "dev.rebel.chatmate.gui.GuiFactory")
public class ChatMate {
  private final YtChatService ytChatService;
  private final YtChatEventService ytChatEventService;
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
    TextUtilityService textUtilityService = new TextUtilityService();

    String apiPath = "http://localhost:3010/api/";
    YtChatProxy ytChatProxy = new YtChatProxy(apiPath);

    String filterPath = "/assets/chatmate/filter.txt";
    FilterService filterService = new FilterService(textUtilityService, '*', filterPath);

    SoundService soundService = new SoundService();

    this.ytChatEventService = new YtChatEventService();
    this.ytChatService = new YtChatService(ytChatProxy, ytChatEventService);
    this.mcChatService = new McChatService(loggingService, filterService, soundService, textUtilityService);
  }

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) {
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register(new EventHandler(this));

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

    ytChatEventService.onChat(this::onNewYtChat);
    ytChatService.start();
    this._enabled = true;
  }

  public void disable() {
    if (!this._enabled) {
      return;
    }

    ytChatEventService.clear();
    ytChatService.stop();
    this._enabled = false;
  }

  private void onNewYtChat(ChatItem[] newChat) {
    for (ChatItem chat: newChat) {
      this.mcChatService.addToMcChat(chat);
    }
  }
}
