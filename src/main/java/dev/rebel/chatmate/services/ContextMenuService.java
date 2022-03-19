package dev.rebel.chatmate.services;

import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import dev.rebel.chatmate.gui.ContextMenu.ContextMenuOption;
import dev.rebel.chatmate.gui.ContextMenuStore;
import dev.rebel.chatmate.gui.Interactive.CountdownModal;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen;
import dev.rebel.chatmate.gui.Interactive.ManageExperienceModal;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.services.events.KeyboardEventService;
import dev.rebel.chatmate.services.events.MouseEventService;
import net.minecraft.client.Minecraft;

public class ContextMenuService {
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final ContextMenuStore store;
  private final ExperienceEndpointProxy experienceEndpointProxy;
  private final McChatService mcChatService;
  private final MouseEventService mouseEventService;
  private final KeyboardEventService keyboardEventService;
  private final ClipboardService clipboardService;
  private final SoundService soundService;
  private final CountdownHandler countdownHandler;

  public ContextMenuService(Minecraft minecraft,
                            DimFactory dimFactory,
                            ContextMenuStore store,
                            ExperienceEndpointProxy experienceEndpointProxy,
                            McChatService mcChatService,
                            MouseEventService mouseEventService,
                            KeyboardEventService keyboardEventService,
                            ClipboardService clipboardService,
                            SoundService soundService,
                            CountdownHandler countdownHandler) {
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;
    this.store = store;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.mcChatService = mcChatService;
    this.mouseEventService = mouseEventService;
    this.keyboardEventService = keyboardEventService;
    this.clipboardService = clipboardService;
    this.soundService = soundService;
    this.countdownHandler = countdownHandler;
  }

  public void showUserContext(Dim x, Dim y, PublicUser user) {
    this.store.showContextMenu(x, y,
      new ContextMenuOption("Reveal on leaderboard", () -> this.onRevealOnLeaderboard(user)),
      new ContextMenuOption("Manage experience", () -> this.onModifyExperience(user))
    );
  }

  public void showHudContext(Dim x, Dim y) {
    this.store.showContextMenu(x, y,
      new ContextMenuOption("Countdown settings", this::onCountdown)
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

  private void onCountdown() {
    InteractiveScreen.InteractiveContext context = this.createInteractiveContext();
    InteractiveScreen screen = new InteractiveScreen(context, this.minecraft.currentScreen);
    IElement modal = new CountdownModal(context, screen, this.countdownHandler);
    screen.setMainElement(modal);
    this.minecraft.displayGuiScreen(screen);
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
