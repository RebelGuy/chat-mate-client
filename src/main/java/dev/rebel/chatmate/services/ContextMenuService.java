package dev.rebel.chatmate.services;

import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.gui.ContextMenu.ContextMenuOption;
import dev.rebel.chatmate.gui.ContextMenuStore;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.models.AbstractChatLine;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.proxy.PunishmentEndpointProxy;
import dev.rebel.chatmate.services.events.KeyboardEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import net.minecraft.client.Minecraft;

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
                            MinecraftProxyService minecraftProxyService) {
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
  }

  public void showUserContext(Dim x, Dim y, PublicUser user) {
    this.store.showContextMenu(x, y,
      new ContextMenuOption("Reveal on leaderboard", () -> this.onRevealOnLeaderboard(user)),
      new ContextMenuOption("Manage experience", () -> this.onModifyExperience(user)),
      new ContextMenuOption("Manage punishments", () -> this.onManagePunishments(user))
    );
  }

  public void showHudContext(Dim x, Dim y) {
    this.store.showContextMenu(x, y,
      new ContextMenuOption("Add countdown title", this::onCountdown),
      new ContextMenuOption("Add counter component", this::onCounter)
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
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen);
    IElement modal = new ManageExperienceModal(context, screen, user, this.experienceEndpointProxy, this.mcChatService);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onManagePunishments(PublicUser user) {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen);
    IElement modal = new ManagePunishmentsModal(context, screen, user, this.punishmentEndpointProxy);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onCountdown() {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen);
    IElement modal = new CountdownModal(context, screen, this.countdownHandler);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onCounter() {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen);
    IElement modal = new CounterModal(context, screen, this.counterHandler);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
  }

  private void onHideMessage(AbstractChatLine chatLine) {
    this.minecraftProxyService.getChatGUI().deleteLine(chatLine);
  }

  private InteractiveScreen.InteractiveContext createInteractiveContext() {
    return new InteractiveScreen.InteractiveContext(this.mouseEventService,
        this.keyboardEventService,
        this.dimFactory,
        this.minecraft,
        this.minecraft.fontRendererObj,
        this.clipboardService,
        this.soundService);
  }
}
