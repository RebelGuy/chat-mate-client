package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.api.models.user.SearchUserRequest;
import dev.rebel.chatmate.api.proxy.UserEndpointProxy;
import dev.rebel.chatmate.services.McChatService;

import javax.annotation.Nonnull;

public class SearchHandler {
  private final UserEndpointProxy userEndpointProxy;
  private final McChatService mcChatService;

  public SearchHandler(UserEndpointProxy userEndpointProxy, McChatService mcChatService) {

    this.userEndpointProxy = userEndpointProxy;
    this.mcChatService = mcChatService;
  }

  public void search(@Nonnull String searchTerm) {

    SearchUserRequest request = new SearchUserRequest(searchTerm);
      this.userEndpointProxy.searchUser(request, res -> this.mcChatService.printUserList(res.results), this.mcChatService::printError);
  }}
