package dev.rebel.chatmate.gui.Interactive.rank;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.TableElement.Column;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange.Platform;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.api.proxy.RankEndpointProxy;
import dev.rebel.chatmate.stores.RankApiStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class Adapters {
  public final EndpointAdapter endpointAdapter;
  public final TableAdapter tableAdapter;
  public final CreateAdapter createAdapter;
  public final DetailsAdapter detailsAdapter;
  public final ChannelRankChangeAdapter channelRankChangeAdapter;

  public Adapters(EndpointAdapter endpointAdapter, TableAdapter tableAdapter, CreateAdapter createAdapter, DetailsAdapter detailsAdapter, ChannelRankChangeAdapter channelRankChangeAdapter) {
    this.endpointAdapter = endpointAdapter;
    this.tableAdapter = tableAdapter;
    this.createAdapter = createAdapter;
    this.detailsAdapter = detailsAdapter;
    this.channelRankChangeAdapter = channelRankChangeAdapter;
  }

  public String getTitle(PublicUser user) {
    return "Manage Ranks for " + user.channelInfo.channelName;
  }

  public static abstract class EndpointAdapter {
    private final RankEndpointProxy rankEndpointProxy;
    private final RankApiStore rankApiStore;

    public EndpointAdapter(RankEndpointProxy proxy, RankApiStore rankApiStore) {
      this.rankEndpointProxy = proxy;
      this.rankApiStore = rankApiStore;
    }

    public final void getAccessibleRanksAsync(Consumer<PublicRank[]> onLoad, @Nullable Consumer<Throwable> onError) {
      this.rankEndpointProxy.getAccessibleRanksAsync(r -> onLoad.accept(r.accessibleRanks), onError);
    }

    // I don't quite know why we are invalidating the store everywhere
    public final void getRanks(int userId, Consumer<List<PublicUserRank>> onLoad, @Nullable Consumer<Throwable> onError) {
      this._getRanks(
          userId,
          ranks -> {
            this.rankApiStore.invalidateUserRanks(userId);
            onLoad.accept(ranks);
          }, error -> {
            this.rankApiStore.invalidateUserRanks(userId);
            onError.accept(error);
          });
    }

    public final void createRank(int userId, RankName rank, @Nullable String createMessage, @Nullable Integer durationSeconds, Consumer<RankResult> onResult, Consumer<Throwable> onError) {
      this._createRank(
          userId,
          rank,
          createMessage,
          durationSeconds,
          result -> {
            this.rankApiStore.invalidateUserRanks(userId);
            onResult.accept(result);
          }, error -> {
            this.rankApiStore.invalidateUserRanks(userId);
            onError.accept(error);
          }
      );
    }

    public void revokeRank(int userId, RankName rank, @Nullable String revokeMessage, Consumer<RankResult> onResult, Consumer<Throwable> onError) {
      this._revokeRank(
          userId,
          rank,
          revokeMessage,
          result -> {
            this.rankApiStore.invalidateUserRanks(userId);
            onResult.accept(result);
          }, error -> {
            this.rankApiStore.invalidateUserRanks(userId);
            onError.accept(error);
          }
      );
    }

    protected abstract void _getRanks(int userId, Consumer<List<PublicUserRank>> onLoad, @Nullable Consumer<Throwable> onError);
    protected abstract void _createRank(int userId, RankName rank, @Nullable String createMessage, @Nullable Integer durationSeconds, Consumer<RankResult> onResult, Consumer<Throwable> onError);
    protected abstract void _revokeRank(int userId, RankName rank, @Nullable String revokeMessage, Consumer<RankResult> onResult, Consumer<Throwable> onError);

    public static class RankResult {
      public final @Nullable PublicUserRank rank;
      public final @Nullable String rankError;
      public final PublicChannelRankChange[] channelRankChanges;

      public RankResult(@Nullable PublicUserRank rank, @Nullable String rankError, PublicChannelRankChange[] channelRankChanges) {
        this.rank = rank;
        this.rankError = rankError;
        this.channelRankChanges = channelRankChanges;
      }
    }
  }

  public static abstract class TableAdapter {
    public String tableHeader = "All Ranks";
    public String loadingRanksMessage = "Loading ranks...";
    public String noRanksMessage = "No ranks to show.";
    public float fontScale = 0.75f;

    public String getLoadingFailedMessage(String apiErrorMessage) { return "Failed to load ranks: " + apiErrorMessage; };
    public abstract List<Column> getColumns();
    public abstract List<IElement> getRow(InteractiveContext context, IElement parent, PublicUserRank rank);
  }

  public static class DetailsAdapter {
    public String getHeader(PublicRank rank) {
      return String.format("Details for %s rank", rank.displayNameNoun.toLowerCase());
    }
  }

  public static abstract class CreateAdapter {
    public abstract boolean shouldIncludeRank(PublicRank rank);
    public abstract String getTitle(PublicRank rank);
    public abstract String getMessagePlaceholder(PublicRank rank);
    public abstract boolean allowExpiration(RankName rank);
    public abstract boolean showClearChatCheckbox(RankName rank);

    public String getExpirationSubtitle(PublicRank rank) {
      return this.allowExpiration(rank.name) ? "(leave blank for permanent rank)" : "";
    }

    public boolean validateExpirationTime(RankName rank, int seconds) {
      return this.allowExpiration(rank) && seconds >= 0;
    }
  }

  public static class ChannelRankChangeAdapter {
    public String internalRankErrorHeaderMessage = "Failed to apply the rank internally:";
    public String noActionsMessage = "No external actions were applied.";
    public String actionsHeaderMessage = "External actions:";

    public String getTooltip(@Nonnull PublicRank underlyingRank, @Nullable PublicUserRank rank, PublicChannelRankChange rankChange) {
      String platform = rankChange.platform == Platform.YOUTUBE ? "YouTube" : "Twitch";
      if (rankChange.error == null) {
        String actionType = rank == null || rank.isActive ? "applied" : "revoked";
        return String.format("Successfully %s action to %s channel %d.", actionType, platform, rankChange.channelId);
      } else {
        String actionType = rank == null || rank.isActive ? "apply" : "revoke";
        return String.format("Failed to %s action to %s channel %d: %s", actionType, platform, rankChange.channelId, rankChange.error);
      }
    }
  }
}
