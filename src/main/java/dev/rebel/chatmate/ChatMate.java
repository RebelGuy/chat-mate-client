package dev.rebel.chatmate;

import dev.rebel.chatmate.commands.ChatMateCommand;
import dev.rebel.chatmate.commands.CountdownCommand;
import dev.rebel.chatmate.commands.CounterCommand;
import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.gui.GuiChatMateHud;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.util.FileHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;

// refer to mcmod.info for more settings.
@Mod(modid = "chatmate", useMetadata = true, canBeDeactivated = true)
public class ChatMate {
  private final ForgeEventService forgeEventService;
  private final YtChatService ytChatService;
  private final McChatService mcChatService;
  private final GuiService guiService;
  private final RenderService renderService;
  private final KeyBindingService keyBindingService;
  private final Config config;

  // from https://forums.minecraftforge.net/topic/68571-1122-check-if-environment-is-deobfuscated/
  final boolean isDev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

  public ChatMate() throws Exception {
    Minecraft minecraft = Minecraft.getMinecraft();
    this.config = new Config();
    this.forgeEventService = new ForgeEventService(minecraft);

    LoggingService loggingService = new LoggingService("log.log", false);

    String apiPath = "http://localhost:3010/api";
    ChatEndpointProxy chatEndpointProxy = new ChatEndpointProxy(loggingService, apiPath);
    ChatMateEndpointProxy chatMateEndpointProxy = new ChatMateEndpointProxy(loggingService, apiPath);

    String filterPath = "/assets/chatmate/filter.txt";
    FilterFileParseResult parsedFilterFile = FilterService.parseFilterFile(FileHelpers.readLines(filterPath));
    FilterService filterService = new FilterService(parsedFilterFile.filtered, parsedFilterFile.whitelisted);

    this.ytChatService = new YtChatService(this.config, chatEndpointProxy);

    SoundService soundService = new SoundService(this.config);
    this.mcChatService = new McChatService(minecraft, loggingService, filterService, soundService);
    StatusService statusService = new StatusService(this.config, chatMateEndpointProxy);

    this.renderService = new RenderService(minecraft, this.forgeEventService);
    this.keyBindingService = new KeyBindingService(this.forgeEventService);
    GuiChatMateHud guiChatMateHud = new GuiChatMateHud(minecraft, this.forgeEventService, statusService);
    this.guiService = new GuiService(this.config, this.forgeEventService, this.keyBindingService, minecraft, guiChatMateHud, soundService);

    ChatMateCommand chatMateCommand = new ChatMateCommand(
      new CountdownCommand(new CountdownHandler(minecraft)),
      new CounterCommand(new CounterHandler(this.keyBindingService, this.renderService))
    );
    ClientCommandHandler.instance.registerCommand(chatMateCommand);

    ytChatService.listen(this::onNewYtChat);
  }

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) {
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register(this.forgeEventService);

    // to make our life easier, auto enable when in a dev environment
    if (this.isDev) {
      this.config.getApiEnabled().set(true);
    }
  }

  // this doesn't do anything!
  @Mod.EventHandler
  public void onFMLDeactivation(FMLModDisabledEvent event) {
    this.config.getApiEnabled().set(false);
  }

  private void onNewYtChat(ChatItem[] newChat) {
    for (ChatItem chat: newChat) {
      this.mcChatService.addToMcChat(chat);
    }
  }
}
