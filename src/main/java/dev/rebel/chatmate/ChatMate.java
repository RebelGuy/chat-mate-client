package dev.rebel.chatmate;

import dev.rebel.chatmate.Environment.Env;
import dev.rebel.chatmate.api.ChatMateApiException;
import dev.rebel.chatmate.api.publicObjects.streamer.PublicStreamerSummary;
import dev.rebel.chatmate.commands.*;
import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.commands.handlers.RanksHandler;
import dev.rebel.chatmate.commands.handlers.SearchHandler;
import dev.rebel.chatmate.config.serialised.SerialisedConfigV6;
import dev.rebel.chatmate.gui.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.*;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.config.ConfigPersistorService;
import dev.rebel.chatmate.api.proxy.*;
import dev.rebel.chatmate.services.*;
import dev.rebel.chatmate.services.FilterService.FilterFileParseResult;
import dev.rebel.chatmate.events.*;
import dev.rebel.chatmate.stores.*;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.util.FileHelpers;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.util.ApiPollerFactory;
import dev.rebel.chatmate.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static dev.rebel.chatmate.util.Objects.ifNotNull;

// refer to mcmod.info for more settings.
@Mod(modid = "chatmate", useMetadata = true, canBeDeactivated = true)
public class ChatMate {
  // from https://forums.minecraftforge.net/topic/68571-1122-check-if-environment-is-deobfuscated/
  final boolean isDev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

  // must hold on to reference in these classes, otherwise they may be garbage collected (wtf)
  ChatMateChatService chatMateChatService;
  DonationHudService donationHudService;

  AccountEndpointProxy accountEndpointProxy;
  Config config;

  @Mod.EventHandler
  public void onFMLInitialization(FMLInitializationEvent event) throws Exception {
    String currentDir = System.getProperty("user.dir").replace("\\", "/");
    String dataDir = currentDir + "/mods/ChatMate";
    FileService fileService = new FileService(dataDir);
    LogService logService = new LogService(fileService);

    Minecraft minecraft = Minecraft.getMinecraft();
    DimFactory dimFactory = new DimFactory(minecraft);

    ConfigPersistorService<SerialisedConfigV6> configPersistorService = new ConfigPersistorService<>(SerialisedConfigV6.class, logService, fileService);
    this.config = new Config(logService, configPersistorService, dimFactory);
    logService.injectConfig(this.config);

    ForgeEventService forgeEventService = new ForgeEventService(logService, minecraft);
    // the "event bus" is the pipeline through which all evens run - so we must register our handler to that
    MinecraftForge.EVENT_BUS.register(forgeEventService);
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
    MouseEventService mouseEventService = new MouseEventService(logService, forgeEventService, minecraft, dimFactory);
    KeyboardEventService keyboardEventService = new KeyboardEventService(logService, forgeEventService);

    String environmentPath = "/environment.yml";
    Environment environment = Environment.parseEnvironmentFile(FileHelpers.readLines(environmentPath));

    String apiPath = environment.serverUrl + "/api";
    DateTimeService dateTimeService = new DateTimeService();
    CursorService cursorService = new CursorService(minecraft, logService, forgeEventService);
    ApiRequestService apiRequestService = new ApiRequestService(cursorService, config);
    ChatEndpointProxy chatEndpointProxy = new ChatEndpointProxy(logService, apiRequestService, apiPath);
    this.accountEndpointProxy = new AccountEndpointProxy(logService, apiRequestService, apiPath);
    this.validateLoginDetails();

    StreamerEndpointProxy streamerEndpointProxy = new StreamerEndpointProxy(logService, apiRequestService, apiPath);
    UserEndpointProxy userEndpointProxy = new UserEndpointProxy(logService, apiRequestService, apiPath);
    ExperienceEndpointProxy experienceEndpointProxy = new ExperienceEndpointProxy(logService, apiRequestService, apiPath);
    PunishmentEndpointProxy punishmentEndpointProxy = new PunishmentEndpointProxy(logService, apiRequestService, apiPath);
    RankEndpointProxy rankEndpointProxy = new RankEndpointProxy(logService, apiRequestService, apiPath);
    DonationEndpointProxy donationEndpointProxy = new DonationEndpointProxy(logService, apiRequestService, apiPath);
    LivestreamEndpointProxy livestreamEndpointProxy = new LivestreamEndpointProxy(logService, apiRequestService, apiPath);

    LivestreamApiStore livestreamApiStore = new LivestreamApiStore(livestreamEndpointProxy, config);
    DonationApiStore donationApiStore = new DonationApiStore(donationEndpointProxy, config);
    RankApiStore rankApiStore = new RankApiStore(rankEndpointProxy, config);
    CommandApiStore commandApiStore = new CommandApiStore(chatEndpointProxy, config);
    StreamerApiStore streamerApiStore = new StreamerApiStore(streamerEndpointProxy);

    String filterPath = "/assets/chatmate/filter.txt";
    FilterFileParseResult parsedFilterFile = FilterService.parseFilterFile(FileHelpers.readLines(filterPath));
    FilterService filterService = new FilterService(parsedFilterFile.filtered, parsedFilterFile.whitelisted);

    ApiPollerFactory apiPollerFactory = new ApiPollerFactory(logService, config, streamerApiStore);
    this.chatMateChatService = new ChatMateChatService(logService, chatEndpointProxy, apiPollerFactory, config, dateTimeService);

    ContextMenuStore contextMenuStore = new ContextMenuStore(minecraft, forgeEventService, mouseEventService, dimFactory, fontEngine);
    ChatComponentRenderer chatComponentRenderer = new ChatComponentRenderer(dimFactory, fontEngine, minecraft);
    MinecraftChatEventService minecraftChatEventService = new MinecraftChatEventService(logService);
    CustomGuiNewChat customGuiNewChat = new CustomGuiNewChat(
        minecraft,
        logService,
        config,
        forgeEventService,
        dimFactory,
        mouseEventService,
        contextMenuStore,
        fontEngine,
        chatComponentRenderer,
        minecraftChatEventService);
    MinecraftProxyService minecraftProxyService = new MinecraftProxyService(minecraft, logService, forgeEventService, customGuiNewChat);

    SoundService soundService = new SoundService(logService, minecraftProxyService, config);
    ChatMateEventService chatMateEventService = new ChatMateEventService(logService, streamerEndpointProxy, apiPollerFactory, config, dateTimeService);
    DonationService donationService = new DonationService(dateTimeService, donationApiStore, livestreamApiStore, rankApiStore, chatMateEventService);
    MessageService messageService = new MessageService(logService, fontEngine, dimFactory, donationService, rankApiStore, chatComponentRenderer, dateTimeService);
    ImageService imageService = new ImageService(minecraft);
    StatusService statusService = new StatusService(streamerEndpointProxy, apiPollerFactory, livestreamApiStore);

    KeyBindingService keyBindingService = new KeyBindingService(forgeEventService);
    ClipboardService clipboardService = new ClipboardService();
    UrlService urlService = new UrlService(logService);
    MinecraftChatService minecraftChatService = new MinecraftChatService(customGuiNewChat);

    DonationHudStore donationHudStore = new DonationHudStore(config, logService);
    Supplier<InteractiveContext> interactiveContextFactory = () -> new InteractiveContext(
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
        forgeEventService,
        chatComponentRenderer,
        rankApiStore,
        livestreamApiStore,
        donationApiStore,
        commandApiStore,
        streamerApiStore,
        config,
        imageService,
        donationHudStore);

    McChatService mcChatService = new McChatService(minecraftProxyService,
        logService,
        filterService,
        soundService,
        chatMateEventService,
        messageService,
        imageService,
        config,
        chatMateChatService,
        fontEngine,
        dimFactory,
        customGuiNewChat,
        minecraftChatEventService,
        interactiveContextFactory.get()
    );

    InteractiveContext hudContext = interactiveContextFactory.get();
    ChatMateHudStore chatMateHudStore = new ChatMateHudStore(hudContext);
    CountdownHandler countdownHandler = new CountdownHandler(dimFactory, minecraft, fontEngine, chatMateHudStore);
    CounterHandler counterHandler = new CounterHandler(keyBindingService, chatMateHudStore, dimFactory);
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
        forgeEventService,
        chatComponentRenderer,
        donationHudStore,
        rankApiStore,
        commandApiStore,
        streamerApiStore,
        livestreamApiStore,
        donationApiStore,
        customGuiNewChat,
        config,
        chatMateHudStore,
        statusService,
        imageService);
    ChatMateHudScreen chatMateHudScreen = new ChatMateHudScreen(chatMateHudStore, contextMenuService, hudContext, config);
    ChatMateHudService chatMateHudService = new ChatMateHudService(chatMateHudStore, dimFactory, config, statusService);

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
        streamerEndpointProxy,
        environment,
        minecraftChatService,
        customGuiIngame,
        fontEngine,
        fontEngineProxy,
        donationEndpointProxy,
        chatMateHudScreen,
        chatComponentRenderer,
        statusService,
        apiRequestService,
        userEndpointProxy,
        messageService,
        livestreamApiStore,
        donationApiStore,
        rankApiStore,
        commandApiStore,
        streamerApiStore,
        customGuiNewChat,
        imageService,
        donationHudStore,
        chatMateHudService,
        accountEndpointProxy,
        fileService.dataFolder);
    this.donationHudService = new DonationHudService(chatMateHudStore,
        donationHudStore,
        guiService,
        dimFactory,
        soundService,
        chatMateEventService,
        logService,
        donationApiStore);

    ChatMateCommand chatMateCommand = new ChatMateCommand(
      new CountdownCommand(countdownHandler),
      new CounterCommand(counterHandler),
      new RanksCommand(new RanksHandler(mcChatService, experienceEndpointProxy)),
      new SearchCommand(new SearchHandler(userEndpointProxy, mcChatService))
    );
    ClientCommandHandler.instance.registerCommand(chatMateCommand);

    apiRequestService.setGetStreamers(() -> ifNotNull(streamerApiStore.getData(), d -> d.streamers));

    config.getChatMateEnabledEmitter().onChange(e -> {
      boolean enabled = e.getData();
      if (enabled) {
        String releaseLabel = "";
        if (environment.env == Env.LOCAL) {
          releaseLabel = "Local build ";
        } else if (environment.env == Env.DEBUG) {
          releaseLabel = "Sandbox build ";
        }

        mcChatService.printInfo(String.format("Enabled. %s%s", releaseLabel, environment.buildName));
      }
    });

    // disable ChatMate when logging out, since we hide the checkbox UI to do this manually
    config.getLoginInfoEmitter().onChange(e -> {
      if (e.getData().username == null) {
        config.getChatMateEnabledEmitter().set(false);
      }
    });

    // to make our life easier, auto enable when in a dev environment or if a livestream is running
    if (this.isDev) {
      config.getChatMateEnabledEmitter().set(true);
    } else if (config.getLoginInfoEmitter().get().username != null) {
      streamerEndpointProxy.getStreamersAsync(streamerRes -> {

        String username = config.getLoginInfoEmitter().get().username;
        @Nullable PublicStreamerSummary streamer = Collections.first(Collections.list(streamerRes.streamers), str -> java.util.Objects.equals(str.username, username));
        if (streamer != null && (streamer.isYoutubeLive() || streamer.isTwitchLive())) {
          logService.logInfo(this, "Auto-enabling ChatMate since the logged in user is a streamer that is currently live");
          config.getChatMateEnabledEmitter().set(true);
        }

      }, streamerErr -> {
        logService.logError(this, "Unable to get streamer list during initialisation", streamerErr);
      });
    }
  }

  private void validateLoginDetails() {
    String loginToken = this.config.getLoginInfoEmitter().get().loginToken;
    if (loginToken == null) {
      return;
    }

    // check whether the login token is still valid - if it isn't, we set it to null
    this.accountEndpointProxy.authenticateAsync(
        r -> this.config.getLoginInfoEmitter().set(new Config.LoginInfo(r.username, loginToken)),
        e -> {
          if (Objects.ifClass(ChatMateApiException.class, e, ex -> ex.apiResponseError.errorCode == 401)) {
            this.config.getLoginInfoEmitter().set(new Config.LoginInfo(null, null));
          }
        }
    );
  }
}
