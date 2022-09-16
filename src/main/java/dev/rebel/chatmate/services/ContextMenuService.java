package dev.rebel.chatmate.services;

import dev.rebel.chatmate.Environment;
import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.gui.ChatComponentRenderer;
import dev.rebel.chatmate.gui.ContextMenu.ContextMenuOption;
import dev.rebel.chatmate.gui.ContextMenuStore;
import dev.rebel.chatmate.gui.CustomGuiNewChat;
import dev.rebel.chatmate.gui.FontEngine;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateHud.DonationHudStore;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveScreenType;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.ScreenRenderer;
import dev.rebel.chatmate.gui.Interactive.rank.ManageRanksModal;
import dev.rebel.chatmate.gui.Interactive.rank.PunishmentAdapters;
import dev.rebel.chatmate.gui.Interactive.rank.RankAdapters;
import dev.rebel.chatmate.gui.models.AbstractChatLine;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.publicObjects.event.PublicDonationData;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.proxy.PunishmentEndpointProxy;
import dev.rebel.chatmate.proxy.RankEndpointProxy;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.KeyboardEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import dev.rebel.chatmate.store.DonationApiStore;
import dev.rebel.chatmate.store.LivestreamApiStore;
import dev.rebel.chatmate.store.RankApiStore;
import net.minecraft.client.Minecraft;

import java.util.Date;

public class ContextMenuService {
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final ContextMenuStore store;
  private final ExperienceEndpointProxy experienceEndpointProxy;
  private final PunishmentEndpointProxy punishmentEndpointProxy;
  private final McChatService mcChatService;
  private final MouseEventService mouseEventService;
  private final KeyboardEventService keyboardEventService;
  private final ClipboardService clipboardService;
  private final SoundService soundService;
  private final CountdownHandler countdownHandler;
  private final CounterHandler counterHandler;
  private final MinecraftProxyService minecraftProxyService;
  private final CursorService cursorService;
  private final UrlService urlService;
  private final Environment environment;
  private final LogService logService;
  private final RankEndpointProxy rankEndpointProxy;
  private final MinecraftChatService minecraftChatService;
  private final FontEngine fontEngine;
  private final ForgeEventService forgeEventService;
  private final ChatComponentRenderer chatComponentRenderer;
  private final DonationHudStore donationHudStore;
  private final RankApiStore rankApiStore;
  private final LivestreamApiStore livestreamApiStore;
  private final DonationApiStore donationApiStore;
  private final CustomGuiNewChat customGuiNewChat;

  public ContextMenuService(Minecraft minecraft,
                            DimFactory dimFactory,
                            ContextMenuStore store,
                            ExperienceEndpointProxy experienceEndpointProxy,
                            PunishmentEndpointProxy punishmentEndpointProxy,
                            McChatService mcChatService,
                            MouseEventService mouseEventService,
                            KeyboardEventService keyboardEventService,
                            ClipboardService clipboardService,
                            SoundService soundService,
                            CountdownHandler countdownHandler,
                            CounterHandler counterHandler,
                            MinecraftProxyService minecraftProxyService,
                            CursorService cursorService,
                            UrlService urlService,
                            Environment environment,
                            LogService logService,
                            RankEndpointProxy rankEndpointProxy,
                            MinecraftChatService minecraftChatService,
                            FontEngine fontEngine,
                            ForgeEventService forgeEventService,
                            ChatComponentRenderer chatComponentRenderer,
                            DonationHudStore donationHudStore,
                            RankApiStore rankApiStore,
                            LivestreamApiStore livestreamApiStore,
                            DonationApiStore donationApiStore,
                            CustomGuiNewChat customGuiNewChat) {
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;
    this.store = store;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.punishmentEndpointProxy = punishmentEndpointProxy;
    this.mcChatService = mcChatService;
    this.mouseEventService = mouseEventService;
    this.keyboardEventService = keyboardEventService;
    this.clipboardService = clipboardService;
    this.soundService = soundService;
    this.countdownHandler = countdownHandler;
    this.counterHandler = counterHandler;
    this.minecraftProxyService = minecraftProxyService;
    this.cursorService = cursorService;
    this.urlService = urlService;
    this.environment = environment;
    this.logService = logService;
    this.rankEndpointProxy = rankEndpointProxy;
    this.minecraftChatService = minecraftChatService;
    this.fontEngine = fontEngine;
    this.forgeEventService = forgeEventService;
    this.chatComponentRenderer = chatComponentRenderer;
    this.donationHudStore = donationHudStore;
    this.rankApiStore = rankApiStore;
    this.livestreamApiStore = livestreamApiStore;
    this.donationApiStore = donationApiStore;
    this.customGuiNewChat = customGuiNewChat;
  }

  public void showUserContext(Dim x, Dim y, PublicUser user) {
    this.store.showContextMenu(x, y,
      new ContextMenuOption("Reveal on leaderboard", () -> this.onRevealOnLeaderboard(user)),
      new ContextMenuOption("Manage experience", () -> this.onModifyExperience(user)),
      new ContextMenuOption("Manage ranks", () -> this.onManageRanks(user)),
      new ContextMenuOption("Manage punishments", () -> this.onManagePunishments(user))
    );
  }

  public void showHudContext(Dim x, Dim y) {
    this.store.showContextMenu(x, y,
      new ContextMenuOption("Add countdown title", this::onCountdown),
      new ContextMenuOption("Add counter component", this::onCounter),
      new ContextMenuOption("Generate fake donation", this::onGenerateFakeDonation)
    );
  }

  public void showChatLineContext(Dim x, Dim y, AbstractChatLine chatLine) {
    this.store.showContextMenu(x, y,
        new ContextMenuOption("Hide message", () -> this.onHideMessage(chatLine))
    );
  }

  private void onRevealOnLeaderboard(PublicUser user) {
    this.experienceEndpointProxy.getRankAsync(user.id, res -> this.mcChatService.printLeaderboard(res.rankedUsers, res.relevantIndex), this.mcChatService::printError);
  }

  private void onModifyExperience(PublicUser user) {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen, InteractiveScreenType.MODAL);
    IElement modal = new ManageExperienceModal(context, screen, user, this.experienceEndpointProxy, this.mcChatService);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onManageRanks(PublicUser user) {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen, InteractiveScreenType.MODAL);
    RankAdapters rankAdapters = new RankAdapters(this.rankEndpointProxy, this.rankApiStore);
    IElement modal = new ManageRanksModal(context, screen, user, rankAdapters);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onManagePunishments(PublicUser user) {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen, InteractiveScreenType.MODAL);
    PunishmentAdapters punishmentAdapters = new PunishmentAdapters(this.rankEndpointProxy, this.punishmentEndpointProxy, this.rankApiStore);
    IElement modal = new ManageRanksModal(context, screen, user, punishmentAdapters);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onCountdown() {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen, InteractiveScreenType.MODAL);
    IElement modal = new CountdownModal(context, screen, this.countdownHandler);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onCounter() {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen, InteractiveScreenType.MODAL);
    IElement modal = new CounterModal(context, screen, this.counterHandler);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onHideMessage(AbstractChatLine chatLine) {
    this.customGuiNewChat.deleteLine(chatLine);
  }

  private void onGenerateFakeDonation() {
    float amount = (float)(Math.random() * 100);
    PublicDonationData donation = new PublicDonationData() {{
      time = new Date().getTime();
      amount = amount;
      formattedAmount = String.format("$%.2f", amount);
      currency = "USD";
      name = "A Donator's Name";
      String msg = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut a";
      message = msg.substring(0, (int)(Math.random() * msg.length()));
    }};
    this.donationHudStore.addDonation(donation);
  }

  private InteractiveScreen.InteractiveContext createInteractiveContext() {
    return new InteractiveScreen.InteractiveContext(new ScreenRenderer(),
        this.mouseEventService,
        this.keyboardEventService,
        this.dimFactory,
        this.minecraft,
        this.fontEngine,
        this.clipboardService,
        this.soundService,
        this.cursorService,
        this.minecraftProxyService,
        this.urlService,
        this.environment,
        this.logService,
        this.minecraftChatService,
        this.forgeEventService,
        this.chatComponentRenderer,
        this.rankApiStore,
        this.livestreamApiStore,
        this.donationApiStore);
  }
}
