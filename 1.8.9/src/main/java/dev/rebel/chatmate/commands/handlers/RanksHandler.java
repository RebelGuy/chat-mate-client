package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.api.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.services.McChatService;

public class RanksHandler {
  private final McChatService mcChatService;
  private final ExperienceEndpointProxy experienceEndpointProxy;

  public RanksHandler(McChatService mcChatService, ExperienceEndpointProxy experienceEndpointProxy) {
    this.mcChatService = mcChatService;
    this.experienceEndpointProxy = experienceEndpointProxy;
  }

  public void onRank() {
    this.experienceEndpointProxy.getLeaderboardAsync(res -> this.mcChatService.printLeaderboard(res.rankedUsers, null), this.mcChatService::printError);
  }
}
