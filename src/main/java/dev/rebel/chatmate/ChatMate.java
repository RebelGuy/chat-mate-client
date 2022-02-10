package dev.rebel.chatmate;

import dev.rebel.chatmate.commands.ChatMateCommand;
import dev.rebel.chatmate.commands.CountdownCommand;
import dev.rebel.chatmate.commands.CounterCommand;
import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.gui.GuiChatMateHud;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.ConfigPersistorService;
import dev.rebel.chatmate.models.chat.GetChatResponse.ChatItem;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV0;
import dev.rebel.chatmate.proxy.ChatEndpointProxy;
import dev.rebel.chatmate.proxy.ChatMateEndpointProxy;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.services.events.*;
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
  private final ChatMateChatService chatMateChatService;
  private final McChatService mcChatService;
  private final MouseEventService mouseEventService;
  private final KeyboardEventService keyboardEventService;
  private final GuiService guiService;
  private final RenderService renderService;
  private final KeyBindingService keyBindingService;
  private final Config config;

  // from https://forums.minecraftforge.net/topic/68571-1122-check-if-environment-is-deobfuscated/
  final boolean isDev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

  public ChatMate() throws Exception {
    String currentDir = System.getProperty("user.dir").replace("\\", "/");
    String dataDir = currentDir + "/mods/ChatMate";
    FileService fileService = new FileService(dataDir);
    LogService logService = new LogService(fileService, false);

    Minecraft minecraft = Minecraft.getMinecraft();
    this.forgeEventService = new ForgeEventService(logService, minecraft);
    MinecraftProxyService minecraftProxyService = new MinecraftProxyService(minecraft, logService, forgeEventService);
    DimFactory dimFactory = new DimFactory(minecraft);

    ConfigPersistorService configPersistorService = new ConfigPersistorService<>(SerialisedConfigV0.class, logService, fileService);
    this.config = new Config(logService, configPersistorService);
    this.mouseEventService = new MouseEventService(logService, forgeEventService, minecraft, dimFactory);
    this.keyboardEventService = new KeyboardEventService(logService, forgeEventService);

    String apiPath = "http://localhost:3010/api";
    ChatEndpointProxy chatEndpointProxy = new ChatEndpointProxy(logService, apiPath);
    ChatMateEndpointProxy chatMateEndpointProxy = new ChatMateEndpointProxy(logService, apiPath);

    String filterPath = "/assets/chatmate/filter.txt";
    FilterFileParseResult parsedFilterFile = FilterService.parseFilterFile(FileHelpers.readLines(filterPath));
    FilterService filterService = new FilterService(parsedFilterFile.filtered, parsedFilterFile.whitelisted);

    this.chatMateChatService = new ChatMateChatService(logService, this.config, chatEndpointProxy);

    SoundService soundService = new SoundService(logService, minecraftProxyService, this.config);
    ChatMateEventService chatMateEventService = new ChatMateEventService(logService, config, chatMateEndpointProxy);
    MessageService messageService = new MessageService();
    this.mcChatService = new McChatService(minecraftProxyService, logService, filterService, soundService, chatMateEventService, messageService);
    StatusService statusService = new StatusService(this.config, chatMateEndpointProxy);

    this.renderService = new RenderService(minecraft, this.forgeEventService);
    this.keyBindingService = new KeyBindingService(this.forgeEventService);
    GuiChatMateHud guiChatMateHud = new GuiChatMateHud(minecraft, dimFactory, this.forgeEventService, statusService, config);
    this.guiService = new GuiService(this.isDev, this.config, this.forgeEventService, this.mouseEventService, this.keyBindingService, minecraft, guiChatMateHud, soundService, dimFactory);

    ChatMateCommand chatMateCommand = new ChatMateCommand(
      new CountdownCommand(new CountdownHandler(minecraft)),
      new CounterCommand(new CounterHandler(this.keyBindingService, this.renderService))
    );
    ClientCommandHandler.instance.registerCommand(chatMateCommand);

    chatMateChatService.onNewChat(this::onNewYtChat, this);
  }

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) {
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register(this.forgeEventService);

    // to make our life easier, auto enable when in a dev environment
    if (this.isDev) {
      this.config.getChatMateEnabledEmitter().set(true);
    }
  }

  // this doesn't do anything!
  @Mod.EventHandler
  public void onFMLDeactivation(FMLModDisabledEvent event) {
    this.config.getChatMateEnabledEmitter().set(false);
  }

  private void onNewYtChat(ChatItem[] newChat) {
    for (ChatItem chat: newChat) {
      this.mcChatService.printStreamChatItem(chat);
    }
  }
}
