package dev.rebel.chatmate;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.proxy.YtChatProxy;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.services.util.FileHelpers;
import net.minecraft.client.Minecraft;
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

  private Config _config;
  public boolean isApiEnabled() { return this._config.isApiEnabled; }
  public boolean isSoundEnabled() { return this._config.isSoundEnabled; }

  // from https://forums.minecraftforge.net/topic/68571-1122-check-if-environment-is-deobfuscated/
  final boolean isDev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

  public ChatMate() throws Exception {
    instance_hack = this;
    this._config = new Config();

    LoggingService loggingService = new LoggingService("log.log", false);

    String apiPath = "http://localhost:3010/api/";
    YtChatProxy ytChatProxy = new YtChatProxy(apiPath);

    String filterPath = "/assets/chatmate/filter.txt";
    FilterFileParseResult parsedFilterFile = FilterService.parseFilterFile(FileHelpers.readLines(filterPath));
    FilterService filterService = new FilterService(parsedFilterFile.filtered, parsedFilterFile.whitelisted);

    SoundService soundService = new SoundService(this._config);

    this.ytChatEventService = new YtChatEventService();
    this.ytChatService = new YtChatService(ytChatProxy, ytChatEventService);

    Minecraft minecraft = Minecraft.getMinecraft();
    this.mcChatService = new McChatService(minecraft, loggingService, filterService, soundService);
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
    if (this.isApiEnabled()) {
      return;
    }

    ytChatEventService.onChat(this::onNewYtChat);
    ytChatService.start();
    this._config.isApiEnabled = true;
  }

  public void disable() {
    if (!this.isApiEnabled()) {
      return;
    }

    ytChatEventService.clear();
    ytChatService.stop();
    this._config.isApiEnabled = false;
  }

  public void enableSound() { this._config.isSoundEnabled = true; }
  public void disableSound() { this._config.isSoundEnabled = false; }

  private void onNewYtChat(ChatItem[] newChat) {
    for (ChatItem chat: newChat) {
      this.mcChatService.addToMcChat(chat);
    }
  }
}
