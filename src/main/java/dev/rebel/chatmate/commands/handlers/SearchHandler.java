package dev.rebel.chatmate.commands.handlers;

import dev.rebel.chatmate.api.models.user.SearchUserRequest;
import dev.rebel.chatmate.api.proxy.UserEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel;
import dev.rebel.chatmate.api.publicObjects.user.PublicUserSearchResults;
import dev.rebel.chatmate.gui.chat.UserSearchResultRowRenderer.AggregatedUserSearchResult;
import dev.rebel.chatmate.services.McChatService;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class SearchHandler {
  private final UserEndpointProxy userEndpointProxy;
  private final McChatService mcChatService;

  public SearchHandler(UserEndpointProxy userEndpointProxy, McChatService mcChatService) {

    this.userEndpointProxy = userEndpointProxy;
    this.mcChatService = mcChatService;
  }

  public void search(@Nonnull String searchTerm) {
    SearchUserRequest request = new SearchUserRequest(searchTerm);
    this.userEndpointProxy.searchUser(request, res -> this.onResponse(res.results), this.mcChatService::printError);
  }

  private void onResponse(PublicUserSearchResults[] results) {
    // wtf. this just makes sure that we have one item per primary user so that we can nicely list all of its channels
    List<List<PublicUserSearchResults>> resultsPerPrimaryUser = Collections.list(Collections.groupBy(Collections.list(results), r -> r.user.primaryUserId).values());
    List<AggregatedUserSearchResult> aggregatedResults = Collections.map(resultsPerPrimaryUser, groupedResults -> {
      List<PublicChannel> matchedChannels = Collections.map(groupedResults, r -> r.matchedChannel);
      List<PublicChannel> allChannels = Collections.flatMap(groupedResults, r -> Collections.list(r.allChannels));
      List<PublicChannel> otherChannels = Collections.filter(allChannels, c -> !Collections.any(matchedChannels, mc -> Objects.equals(mc.channelId, c.channelId) && mc.platform == c.platform));
      List<List<PublicChannel>> otherChannelsGrouped = Collections.list(Collections.groupBy(otherChannels, c -> c.platform.toString() + c.channelId).values());
      otherChannels = Collections.map(otherChannelsGrouped, c -> c.get(0));

      return new AggregatedUserSearchResult(groupedResults.get(0).user, matchedChannels, otherChannels);
    });
    aggregatedResults = Collections.reverse(Collections.orderBy(aggregatedResults, r -> r.user.levelInfo.level + r.user.levelInfo.levelProgress));

    this.mcChatService.printUserSearchResults(aggregatedResults);
  }
}
