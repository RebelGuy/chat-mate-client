package dev.rebel.chatmate;

import dev.rebel.chatmate.commands.*;
import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.commands.handlers.RanksHandler;
import dev.rebel.chatmate.commands.handlers.SearchHandler;
import dev.rebel.chatmate.gui.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudScreen;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.ChatMateHudStore;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.models.ConfigPersistorService;
import dev.rebel.chatmate.models.configMigrations.SerialisedConfigVersions.SerialisedConfigV2;
import dev.rebel.chatmate.proxy.*;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.services.events.*;
import dev.rebel.chatmate.services.util.FileHelpers;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.util.ApiPollerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.function.Supplier;

// refer to mcmod.info for more settings.
@Mod(modid = "chatmate", useMetadata = true, canBeDeactivated = true)
public class ChatMate {
  // from https://forums.minecraftforge.net/topic/68571-1122-check-if-environment-is-deobfuscated/
  final boolean isDev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) throws Exception {
    String currentDir = System.getProperty("user.dir").replace("\\", "/");
    String dataDir = currentDir + "/mods/ChatMate";
    FileService fileService = new FileService(dataDir);
    LogService logService = new LogService(fileService, false);

    Minecraft minecraft = Minecraft.getMinecraft();
    ForgeEventService forgeEventService = new ForgeEventService(logService, minecraft);
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register(forgeEventService);
    MinecraftProxyService minecraftProxyService = new MinecraftProxyService(minecraft, logService, forgeEventService);
    DimFactory dimFactory = new DimFactory(minecraft);
    FontEngine fontEngine = new FontEngine(dimFactory, minecraft.gameSettings, new ResourceLocation("textures/font/ascii.png"), minecraft.renderEngine, false);
    FontEngineProxy fontEngineProxy = new FontEngineProxy(fontEngine, dimFactory, minecraft.gameSettings, new ResourceLocation("textures/font/ascii.png"), minecraft.renderEngine, false);

    // mirror the fontRendererObj operation that are performed within the Minecraft::startGame method, since we missed those
    minecraft.fontRendererObj = fontEngineProxy;
    if (minecraft.gameSettings.language != null) {
      fontEngineProxy.setUnicodeFlag(minecraft.isUnicode());
      fontEngineProxy.setBidiFlag(minecraft.getLanguageManager().isCurrentLanguageBidirectional());
    }
    IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager)minecraft.getResourceManager();
    reloadableResourceManager.registerReloadListener(fontEngineProxy);

    ConfigPersistorService configPersistorService = new ConfigPersistorService<>(SerialisedConfigV2.class, logService, fileService);
    Config config = new Config(logService, configPersistorService);
    MouseEventService mouseEventService = new MouseEventService(logService, forgeEventService, minecraft, dimFactory);
    KeyboardEventService keyboardEventService = new KeyboardEventService(logService, forgeEventService);

    String environmentPath = "/environment.yml";
    Environment environment = Environment.parseEnvironmentFile(FileHelpers.readLines(environmentPath));

    String apiPath = environment.serverUrl + "/api";
    CursorService cursorService = new CursorService(minecraft, logService, forgeEventService);
    ApiRequestService apiRequestService = new ApiRequestService(cursorService);
    ChatEndpointProxy chatEndpointProxy = new ChatEndpointProxy(logService, apiRequestService, apiPath);
    ChatMateEndpointProxy chatMateEndpointProxy = new ChatMateEndpointProxy(logService, apiRequestService, apiPath);
    UserEndpointProxy userEndpointProxy = new UserEndpointProxy(logService, apiRequestService, apiPath);
    ExperienceEndpointProxy experienceEndpointProxy = new ExperienceEndpointProxy(logService, apiRequestService, apiPath);
    PunishmentEndpointProxy punishmentEndpointProxy = new PunishmentEndpointProxy(logService, apiRequestService, apiPath);
    LogEndpointProxy logEndpointProxy = new LogEndpointProxy(logService, apiRequestService, apiPath);
    RankEndpointProxy rankEndpointProxy = new RankEndpointProxy(logService, apiRequestService, apiPath);
    DonationEndpointProxy donationEndpointProxy = new DonationEndpointProxy(logService, apiRequestService, apiPath);

    String filterPath = "/assets/chatmate/filter.txt";
    FilterFileParseResult parsedFilterFile = FilterService.parseFilterFile(FileHelpers.readLines(filterPath));
    FilterService filterService = new FilterService(parsedFilterFile.filtered, parsedFilterFile.whitelisted);

    ApiPollerFactory apiPollerFactory = new ApiPollerFactory(logService, config);
    ChatMateChatService chatMateChatService = new ChatMateChatService(logService, chatEndpointProxy, apiPollerFactory);

    SoundService soundService = new SoundService(logService, minecraftProxyService, config);
    ChatMateEventService chatMateEventService = new ChatMateEventService(logService, chatMateEndpointProxy, apiPollerFactory);
    MessageService messageService = new MessageService(logService, fontEngine);
    ImageService imageService = new ImageService(minecraft);
    McChatService mcChatService = new McChatService(minecraftProxyService,
        logService,
        filterService,
        soundService,
        chatMateEventService,
        messageService,
        imageService,
        config,
        chatMateChatService,
        fontEngine);
    StatusService statusService = new StatusService(chatMateEndpointProxy, apiPollerFactory);

    RenderService renderService = new RenderService(minecraft, forgeEventService, fontEngine, dimFactory);
    KeyBindingService keyBindingService = new KeyBindingService(forgeEventService);
    ServerLogEventService serverLogEventService = new ServerLogEventService(logService, logEndpointProxy, apiPollerFactory);
    GuiChatMateHud guiChatMateHud = new GuiChatMateHud(minecraft, fontEngine, dimFactory, forgeEventService, statusService, config, serverLogEventService);
    ContextMenuStore contextMenuStore = new ContextMenuStore(minecraft, forgeEventService, mouseEventService, dimFactory, fontEngine);
    ClipboardService clipboardService = new ClipboardService();
    UrlService urlService = new UrlService(logService);
    MinecraftChatService minecraftChatService = new MinecraftChatService(minecraftProxyService);

    InteractiveContext hudContext = new InteractiveContext(
        new InteractiveScreen.ScreenRenderer(),
        mouseEventService,
        keyboardEventService,
        dimFactory,
        minecraft,
        fontEngine,
        clipboardService,
        soundService,
        cursorService,
        minecraftProxyService,
        urlService,
        environment,
        logService,
        minecraftChatService,
        forgeEventService
    );

    ChatMateHudStore chatMateHudStore = new ChatMateHudStore(hudContext);
    CountdownHandler countdownHandler = new CountdownHandler(dimFactory, minecraft, fontEngine, guiChatMateHud);
    CounterHandler counterHandler = new CounterHandler(keyBindingService, chatMateHudStore, dimFactory, minecraft, fontEngine);
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
        urlService,
        environment,
        logService,
        rankEndpointProxy,
        minecraftChatService,
        fontEngine,
        forgeEventService);
    ChatMateHudScreen chatMateHudScreen = new ChatMateHudScreen(chatMateHudStore, contextMenuService, hudContext, config);

    ChatComponentRenderer chatComponentRenderer = new ChatComponentRenderer(dimFactory, fontEngine, minecraft);
    CustomGuiNewChat customGuiNewChat = new CustomGuiNewChat(
        minecraft,
        logService,
        config,
        forgeEventService,
        dimFactory,
        mouseEventService,
        contextMenuStore,
        fontEngine,
        chatComponentRenderer);
    CustomGuiIngame customGuiIngame = new CustomGuiIngame(minecraft, customGuiNewChat);
    GuiService guiService = new GuiService(this.isDev,
        logService,
        config,
        forgeEventService,
        mouseEventService,
        keyBindingService,
        minecraft,
        minecraftProxyService,
        soundService,
        dimFactory,
        contextMenuStore,
        contextMenuService,
        cursorService,
        keyboardEventService,
        clipboardService,
        urlService,
        chatMateEndpointProxy,
        environment,
        minecraftChatService,
        customGuiIngame,
        fontEngine,
        fontEngineProxy,
        donationEndpointProxy,
        chatMateHudScreen);

    ChatMateCommand chatMateCommand = new ChatMateCommand(
      new CountdownCommand(countdownHandler),
      new CounterCommand(counterHandler),
      new RanksCommand(new RanksHandler(mcChatService, experienceEndpointProxy)),
      new SearchCommand(new SearchHandler(userEndpointProxy, mcChatService))
    );
    ClientCommandHandler.instance.registerCommand(chatMateCommand);

    config.getChatMateEnabledEmitter().onChange(enabled -> {
      if (enabled) {
        mcChatService.printInfo(String.format("Enabled. [%s]", environment.env.toString().toCharArray()[0]));
      }
    });

    // to make our life easier, auto enable when in a dev environment
    if (this.isDev) {
      config.getChatMateEnabledEmitter().set(true);
    }
  }
}
