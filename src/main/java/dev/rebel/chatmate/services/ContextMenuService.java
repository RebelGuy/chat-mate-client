package dev.rebel.chatmate.services;

import dev.rebel.chatmate.gui.ContextMenu.ContextMenuOption;
import dev.rebel.chatmate.gui.ContextMenuStore;
import dev.rebel.chatmate.gui.GuiManageExperienceScreen;
import dev.rebel.chatmate.gui.models.Dim;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.publicObjects.user.PublicUser;
import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import net.minecraft.client.Minecraft;

public class ContextMenuService {
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final ContextMenuStore store;
  private final ExperienceEndpointProxy experienceEndpointProxy;
  private final McChatService mcChatService;

  public ContextMenuService(Minecraft minecraft, DimFactory dimFactory, ContextMenuStore store, ExperienceEndpointProxy experienceEndpointProxy, McChatService mcChatService) {
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;
    this.store = store;
    this.experienceEndpointProxy = experienceEndpointProxy;
    this.mcChatService = mcChatService;
  }

  public void showUserContext(Dim x, Dim y, PublicUser user) {
    this.store.showContextMenu(x, y,
      new ContextMenuOption("Reveal on leaderboard", () -> this.onRevealOnLeaderboard(user)),
      new ContextMenuOption("Manage experience", () -> this.onModifyExperience(user))
    );
  }

  private void onRevealOnLeaderboard(PublicUser user) {
    this.experienceEndpointProxy.getRankAsync(user.id, res -> this.mcChatService.printLeaderboard(res.rankedUsers, res.relevantIndex), this.mcChatService::printError);
  }

  private void onModifyExperience(PublicUser user) {
    this.minecraft.displayGuiScreen(new GuiManageExperienceScreen(this.minecraft, this.dimFactory, user, this.experienceEndpointProxy, this.mcChatService));
  }
}
