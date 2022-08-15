package dev.rebel.chatmate;

import dev.rebel.chatmate.commands.*;
import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.commands.handlers.RanksHandler;
import dev.rebel.chatmate.commands.handlers.SearchHandler;
import dev.rebel.chatmate.gui.ContextMenuStore;
import dev.rebel.chatmate.gui.GuiChatMateHud;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.ConfigPersistorService;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV2;
import dev.rebel.chatmate.models.publicObjects.chat.PublicChatItem;
import dev.rebel.chatmate.proxy.*;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.services.events.*;
import dev.rebel.chatmate.services.util.FileHelpers;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.util.ApiPollerFactory;
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
  private final Minecraft minecraft;
  private final ForgeEventService forgeEventService;
  private final ChatMateChatService chatMateChatService;
  private final McChatService mcChatService;
  private final MouseEventService mouseEventService;
  private final KeyboardEventService keyboardEventService;
  private final GuiService guiService;
  private final RenderService renderService;
  private final KeyBindingService keyBindingService;
  private final Config config;
  private final Environment environment;

  // from https://forums.minecraftforge.net/topic/68571-1122-check-if-environment-is-deobfuscated/
  final boolean isDev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

  public ChatMate() throws Exception {
    String currentDir = System.getProperty("user.dir").replace("\\", "/");
    String dataDir = currentDir + "/mods/ChatMate";
    FileService fileService = new FileService(dataDir);
    LogService logService = new LogService(fileService, false);

    Minecraft minecraft = Minecraft.getMinecraft();
    this.minecraft = minecraft;
    this.forgeEventService = new ForgeEventService(logService, minecraft);
    MinecraftProxyService minecraftProxyService = new MinecraftProxyService(minecraft, logService, forgeEventService);
    DimFactory dimFactory = new DimFactory(minecraft);

    ConfigPersistorService configPersistorService = new ConfigPersistorService<>(SerialisedConfigV2.class, logService, fileService);
    this.config = new Config(logService, configPersistorService);
    this.mouseEventService = new MouseEventService(logService, forgeEventService, minecraft, dimFactory);
    this.keyboardEventService = new KeyboardEventService(logService, forgeEventService);

    String environmentPath = "/environment.yml";
    this.environment = Environment.parseEnvironmentFile(FileHelpers.readLines(environmentPath));

    String apiPath = this.environment.serverUrl + "/api";
    CursorService cursorService = new CursorService(minecraft, logService, forgeEventService);
    ApiRequestService apiRequestService = new ApiRequestService(cursorService);
    ChatEndpointProxy chatEndpointProxy = new ChatEndpointProxy(logService, apiRequestService, apiPath);
    ChatMateEndpointProxy chatMateEndpointProxy = new ChatMateEndpointProxy(logService, apiRequestService, apiPath);
    UserEndpointProxy userEndpointProxy = new UserEndpointProxy(logService, apiRequestService, apiPath);
    ExperienceEndpointProxy experienceEndpointProxy = new ExperienceEndpointProxy(logService, apiRequestService, apiPath);
    PunishmentEndpointProxy punishmentEndpointProxy = new PunishmentEndpointProxy(logService, apiRequestService, apiPath);
    LogEndpointProxy logEndpointProxy = new LogEndpointProxy(logService, apiRequestService, apiPath);
    RankEndpointProxy rankEndpointProxy = new RankEndpointProxy(logService, apiRequestService, apiPath);

    String filterPath = "/assets/chatmate/filter.txt";
    FilterFileParseResult parsedFilterFile = FilterService.parseFilterFile(FileHelpers.readLines(filterPath));
    FilterService filterService = new FilterService(parsedFilterFile.filtered, parsedFilterFile.whitelisted);

    ApiPollerFactory apiPollerFactory = new ApiPollerFactory(logService, config);
    this.chatMateChatService = new ChatMateChatService(logService, chatEndpointProxy, apiPollerFactory);

    SoundService soundService = new SoundService(logService, minecraftProxyService, this.config);
    ChatMateEventService chatMateEventService = new ChatMateEventService(logService, chatMateEndpointProxy, apiPollerFactory);
    MessageService messageService = new MessageService(logService, minecraftProxyService);
    ImageService imageService = new ImageService(minecraft);
    this.mcChatService = new McChatService(minecraftProxyService,
        logService,
        filterService,
        soundService,
        chatMateEventService,
        messageService,
        imageService,
        config);
    StatusService statusService = new StatusService(chatMateEndpointProxy, apiPollerFactory);

    this.renderService = new RenderService(minecraft, this.forgeEventService);
    this.keyBindingService = new KeyBindingService(this.forgeEventService);
    ServerLogEventService serverLogEventService = new ServerLogEventService(logService, logEndpointProxy, apiPollerFactory);
    GuiChatMateHud guiChatMateHud = new GuiChatMateHud(minecraft, dimFactory, this.forgeEventService, statusService, config, serverLogEventService);
    ContextMenuStore contextMenuStore = new ContextMenuStore(minecraft, this.forgeEventService, this.mouseEventService, dimFactory);
    ClipboardService clipboardService = new ClipboardService();
    CountdownHandler countdownHandler = new CountdownHandler(dimFactory, minecraft, guiChatMateHud);
    CounterHandler counterHandler = new CounterHandler(this.keyBindingService, guiChatMateHud, dimFactory, minecraft);
    BrowserService browserService = new BrowserService(logService);
    MinecraftChatService minecraftChatService = new MinecraftChatService(minecraftProxyService);
    ContextMenuService contextMenuService = new ContextMenuService(minecraft,
        dimFactory,
        contextMenuStore,
        experienceEndpointProxy,
        punishmentEndpointProxy,
        mcChatService,
        mouseEventService,
        keyboardEventService,
        clipboardService,
        soundService,
        countdownHandler,
        counterHandler,
        minecraftProxyService,
        cursorService,
        browserService,
        environment,
        logService,
        rankEndpointProxy,
        minecraftChatService);
    this.guiService = new GuiService(this.isDev,
        logService,
        this.config,
        this.forgeEventService,
        this.mouseEventService,
        this.keyBindingService,
        minecraft,
        minecraftProxyService,
        guiChatMateHud,
        soundService,
        dimFactory,
        contextMenuStore,
        contextMenuService,
        cursorService,
        keyboardEventService,
        clipboardService,
        browserService,
        chatMateEndpointProxy,
        environment,
        minecraftChatService);

    ChatMateCommand chatMateCommand = new ChatMateCommand(
      new CountdownCommand(countdownHandler),
      new CounterCommand(counterHandler),
      new RanksCommand(new RanksHandler(mcChatService, experienceEndpointProxy)),
      new SearchCommand(new SearchHandler(userEndpointProxy, mcChatService))
    );
    ClientCommandHandler.instance.registerCommand(chatMateCommand);

    chatMateChatService.onNewChat(this::onNewYtChat, null);
  }

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) {
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register(this.forgeEventService);

    this.guiService.initialiseCustomChat();
    this.config.getChatMateEnabledEmitter().onChange(enabled -> {
      if (enabled) {
        this.mcChatService.printInfo(String.format("Enabled. [%s]", this.environment.env.toString().toCharArray()[0]));
      }
    });

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

  private void onNewYtChat(PublicChatItem[] newChat) {
    for (PublicChatItem chat: newChat) {
      this.mcChatService.printStreamChatItem(chat);
    }
  }
}
