package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.proxy.ExperienceEndpointProxy;
import dev.rebel.chatmate.services.McChatService;

import javax.annotation.Nullable;

public class RanksHandler {
  private final McChatService mcChatService;
  private final ExperienceEndpointProxy experienceEndpointProxy;

  public RanksHandler(McChatService mcChatService, ExperienceEndpointProxy experienceEndpointProxy) {
    this.mcChatService = mcChatService;
    this.experienceEndpointProxy = experienceEndpointProxy;
  }

  public void onRank(@Nullable String name) {
    if (name == null || name.trim().isEmpty()) {
      this.experienceEndpointProxy.getLeaderboardAsync(res -> this.mcChatService.printLeaderboard(res.rankedUsers, null), this.mcChatService::printError);
    } else {
      this.experienceEndpointProxy.getRankAsync(name, res -> this.mcChatService.printLeaderboard(res.rankedUsers, res.relevantIndex), this.mcChatService::printError);
    }
  }
}
