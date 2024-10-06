package dev.rebel.chatmate.gui.Interactive.rank;

import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.TableElement.Column;
import dev.rebel.chatmate.api.models.rank.AddModRankRequest;
import dev.rebel.chatmate.api.models.rank.AddUserRankRequest;
import dev.rebel.chatmate.api.models.rank.AddUserRankRequest.AddRankName;
import dev.rebel.chatmate.api.models.rank.RemoveModRankRequest;
import dev.rebel.chatmate.api.models.rank.RemoveUserRankRequest;
import dev.rebel.chatmate.api.models.rank.RemoveUserRankRequest.RemoveRankName;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankGroup;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.RankEndpointProxy;
import dev.rebel.chatmate.gui.Interactive.TableElement.RowContents;
import dev.rebel.chatmate.util.Collections;
import dev.rebel.chatmate.stores.RankApiStore;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.TextHelpers.dateToDayAccuracy;
import static dev.rebel.chatmate.util.TextHelpers.toSentenceCase;

public class RankAdapters extends Adapters {
  public RankAdapters(RankEndpointProxy rankEndpointProxy, RankApiStore rankApiStore) {
    super(
      new RankEndpointAdapter(rankEndpointProxy, rankApiStore),
      new RankTableAdapter(),
      new RankCreateAdapter(),
      new DetailsAdapter(),
      new ChannelRankChangeAdapter()
    );
  }

  public static class RankEndpointAdapter extends EndpointAdapter {
    private final RankEndpointProxy rankEndpointProxy;
    private final RankApiStore rankApiStore;

    public RankEndpointAdapter(RankEndpointProxy rankEndpointProxy, RankApiStore rankApiStore) {
      super(rankEndpointProxy, rankApiStore);
      this.rankEndpointProxy = rankEndpointProxy;
      this.rankApiStore = rankApiStore;
    }

    @Override
    protected void _getRanks(int userId, Consumer<List<PublicUserRank>> onLoad, @Nullable Consumer<Throwable> onError) {
      this.rankApiStore.loadUserRanks(userId, ranks -> onLoad.accept(Collections.filter(ranks, r -> r.isActive && r.rank.group != RankGroup.PUNISHMENT)), onError, false);
    }

    @Override
    protected void _createRank(int userId, RankName rank, @Nullable String createMessage, @Nullable Integer durationSeconds, Consumer<RankResult> onResult, Consumer<Throwable> onError) {
      if (rank == RankName.MOD) {
        AddModRankRequest request = new AddModRankRequest(userId, createMessage);
        this.rankEndpointProxy.addModRank(request, r -> onResult.accept(new RankResult(r.newRank, r.newRankError, r.channelModChanges)), onError);
      } else {
        AddRankName rankName;
        if (rank == RankName.FAMOUS) {
          rankName = AddRankName.FAMOUS;
        } else if (rank == RankName.DONATOR) {
          rankName = AddRankName.DONATOR;
        } else if (rank == RankName.SUPPORTER) {
          rankName = AddRankName.SUPPORTER;
        } else if (rank == RankName.MEMBER) {
          rankName = AddRankName.MEMBER;
        } else {
          onError.accept(new Exception("Cannot create invalid rank type " + rank));
          return;
        }
        AddUserRankRequest request = new AddUserRankRequest(rankName, userId, createMessage, durationSeconds == null ? 0 : durationSeconds);
        this.rankEndpointProxy.addUserRank(request, r -> onResult.accept(new RankResult(r.newRank, null, null)), onError);
      }
    }

    @Override
    protected void _revokeRank(int userId, RankName rank, @Nullable String revokeMessage, Consumer<RankResult> onResult, Consumer<Throwable> onError) {
      if (rank == RankName.MOD) {
        RemoveModRankRequest request = new RemoveModRankRequest(userId, revokeMessage);
        this.rankEndpointProxy.removeModRank(request, r -> onResult.accept(new RankResult(r.removedRank, r.removedRankError, r.channelModChanges)), onError);
      } else {
        RemoveRankName rankName;
        if (rank == RankName.FAMOUS) {
          rankName = RemoveRankName.FAMOUS;
        } else if (rank == RankName.DONATOR) {
          rankName = RemoveRankName.DONATOR;
        } else if (rank == RankName.SUPPORTER) {
          rankName = RemoveRankName.SUPPORTER;
        } else if (rank == RankName.MEMBER) {
          rankName = RemoveRankName.MEMBER;
        } else {
          onError.accept(new Exception("Cannot remove invalid rank type " + rank));
          return;
        }
        RemoveUserRankRequest request = new RemoveUserRankRequest(rankName, userId, revokeMessage);
        this.rankEndpointProxy.removeUserRank(request, r -> onResult.accept(new RankResult(r.removedRank, null, null)), onError);
      }
    }
  }

  public static class RankTableAdapter extends TableAdapter {
    @Override
    public List<Column> getColumns() {
      return Collections.list(
          new Column("Type", super.fontScale, 1.5f, true),
          new Column("Date", super.fontScale, 2, true),
          new Column("Message", super.fontScale, 3, false),
          new Column("Perm", super.fontScale, 1, true)
      );
    }

    @Override
    public RowContents<PublicUserRank> getRow(InteractiveContext context, IElement parent, PublicUserRank rank) {
      String dateStr = dateToDayAccuracy(rank.issuedAt);
      List<IElement> rowElements = Collections.list(
          new LabelElement(context, parent).setText(toSentenceCase(rank.rank.displayNameNoun)).setOverflow(TextOverflow.TRUNCATE).setFontScale(super.fontScale).setHorizontalAlignment(HorizontalAlignment.LEFT),
          new LabelElement(context, parent).setText(dateStr).setOverflow(TextOverflow.TRUNCATE).setFontScale(super.fontScale).setHorizontalAlignment(HorizontalAlignment.LEFT),
          new LabelElement(context, parent).setText(rank.message).setOverflow(TextOverflow.SPLIT).setFontScale(super.fontScale).setHorizontalAlignment(HorizontalAlignment.LEFT).setSizingMode(SizingMode.FILL),
          new LabelElement(context, parent).setText(rank.expirationTime == null ? "Yes" : "No").setOverflow(TextOverflow.TRUNCATE).setFontScale(super.fontScale)
      );
      return new RowContents<>(rowElements);
    }
  }

  public static class RankCreateAdapter extends CreateAdapter {
    @Override
    public boolean shouldIncludeRank(PublicRank rank) {
      return rank.group != RankGroup.PUNISHMENT;
    }

    @Override
    public String getTitle(PublicRank rank) {
      return String.format("Apply %s rank", rank.displayNameNoun);
    }

    @Override
    public String getMessagePlaceholder(PublicRank rank) {
      return "Add a note";
    }

    @Override
    public boolean allowExpiration(RankName rank) {
      // user ranks allow expiration
      return rank != RankName.MOD;
    }

    @Override
    public boolean showClearChatCheckbox(RankName rank) {
      return false;
    }
  }
}
