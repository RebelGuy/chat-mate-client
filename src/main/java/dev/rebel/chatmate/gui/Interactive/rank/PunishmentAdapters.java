package dev.rebel.chatmate.gui.Interactive.rank;

import dev.rebel.chatmate.api.models.punishment.*;
import dev.rebel.chatmate.api.proxy.PunishmentEndpointProxy;
import dev.rebel.chatmate.api.proxy.RankEndpointProxy;
import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankGroup;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank.RankName;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.publicObjects.user.PublicChannel.Platform;
import dev.rebel.chatmate.api.publicObjects.user.PublicUser;
import dev.rebel.chatmate.gui.Interactive.IElement;
import dev.rebel.chatmate.gui.Interactive.InteractiveScreen.InteractiveContext;
import dev.rebel.chatmate.gui.Interactive.LabelElement;
import dev.rebel.chatmate.gui.Interactive.LabelElement.TextOverflow;
import dev.rebel.chatmate.gui.Interactive.Layout.HorizontalAlignment;
import dev.rebel.chatmate.gui.Interactive.Layout.SizingMode;
import dev.rebel.chatmate.gui.Interactive.TableElement.Column;
import dev.rebel.chatmate.gui.Interactive.TableElement.RowContents;
import dev.rebel.chatmate.stores.RankApiStore;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.TextHelpers.dateToDayAccuracy;
import static dev.rebel.chatmate.util.TextHelpers.toSentenceCase;

public class PunishmentAdapters extends Adapters {
  public PunishmentAdapters(RankEndpointProxy rankEndpointProxy, PunishmentEndpointProxy punishmentEndpointProxy, RankApiStore rankApiStore) {
    super(
      new PunishmentEndpointAdapter(rankEndpointProxy, punishmentEndpointProxy, rankApiStore),
      new PunishmentTableAdapter(),
      new PunishmentCreateAdapter(),
      new PunishmentDetailsAdapter(),
      new PunishmentChannelRankChangeAdapter()
    );
  }

  @Override
  public String getTitle(PublicUser user) {
    return "Manage Punishments for " + user.channel.displayName;
  }

  public static class PunishmentEndpointAdapter extends EndpointAdapter {
    private final PunishmentEndpointProxy punishmentEndpointProxy;

    public PunishmentEndpointAdapter(RankEndpointProxy rankEndpointProxy, PunishmentEndpointProxy punishmentEndpointProxy, RankApiStore rankApiStore) {
      super(rankEndpointProxy, rankApiStore);
      this.punishmentEndpointProxy = punishmentEndpointProxy;
    }

    @Override
    protected void _getRanks(int userId, Consumer<List<PublicUserRank>> onLoad, @Nullable Consumer<Throwable> onError) {
      this.punishmentEndpointProxy.getPunishmentsAsync(userId, true, res -> onLoad.accept(Collections.list(res.punishments)), onError);
    }

    @Override
    protected void _createRank(int userId, RankName rank, @Nullable String createMessage, @Nullable Integer durationSeconds, Consumer<RankResult> onResult, Consumer<Throwable> onError) {
      if (rank == RankName.BAN) {
        BanUserRequest request = new BanUserRequest(userId, createMessage);
        this.punishmentEndpointProxy.banUserAsync(request, r -> onResult.accept(new RankResult(r.newPunishment, r.newPunishmentError, r.channelPunishments)), onError);
      } else if (rank == RankName.TIMEOUT) {
        TimeoutUserRequest request = new TimeoutUserRequest(userId, createMessage, durationSeconds == null ? 0 : durationSeconds);
        this.punishmentEndpointProxy.timeoutUserAsync(request, r -> onResult.accept(new RankResult(r.newPunishment, r.newPunishmentError, r.channelPunishments)), onError);
      } else if (rank == RankName.MUTE) {
        MuteUserRequest request = new MuteUserRequest(userId, createMessage, durationSeconds == null ? 0 : durationSeconds);
        this.punishmentEndpointProxy.muteUserAsync(request, r -> onResult.accept(new RankResult(r.newPunishment, null, new PublicChannelRankChange[0])), onError);
      } else {
        onError.accept(new Exception("Cannot create invalid punishment type " + rank));
      }
    }

    @Override
    protected void _revokeRank(int userId, RankName rank, @Nullable String revokeMessage, Consumer<RankResult> onResult, Consumer<Throwable> onError) {
      if (rank == RankName.BAN) {
        UnbanUserRequest request = new UnbanUserRequest(userId, revokeMessage);
        this.punishmentEndpointProxy.unbanUserAsync(request, r -> onResult.accept(new RankResult(r.removedPunishment, r.removedPunishmentError, r.channelPunishments)), onError);
      } else if (rank == RankName.TIMEOUT) {
        RevokeTimeoutRequest request = new RevokeTimeoutRequest(userId, revokeMessage);
        this.punishmentEndpointProxy.revokeTimeoutAsync(request, r -> onResult.accept(new RankResult(r.removedPunishment, r.removedPunishmentError, r.channelPunishments)), onError);
      } else if (rank == RankName.MUTE) {
        UnmuteUserRequest request = new UnmuteUserRequest(userId, revokeMessage);
        this.punishmentEndpointProxy.unmuteUserAsync(request, r -> onResult.accept(new RankResult(r.removedPunishment, null, new PublicChannelRankChange[0])), onError);
      } else {
        onError.accept(new Exception("Cannot revoke invalid punishment type " + rank));
      }
    }
  }

  public static class PunishmentTableAdapter extends TableAdapter {
    public PunishmentTableAdapter() {
      super.tableHeader = "All Punishments";
      super.loadingRanksMessage = "Loading punishments...";
      super.noRanksMessage = "No punishments to show.";
    }

    @Override
    public String getLoadingFailedMessage(String apiErrorMessage) {
      return "Failed to load punishments: " + apiErrorMessage;
    }

    @Override
    public List<Column> getColumns() {
      return Collections.list(
          new Column("Date", super.fontScale, 2, true),
          new Column("Type", super.fontScale, 1.5f, true),
          new Column("Message", super.fontScale, 3, false),
          new Column("Perm", super.fontScale, 1, true),
          new Column("Active", super.fontScale, 1, true)
      );
    }

    @Override
    public RowContents<PublicUserRank> getRow(InteractiveContext context, IElement parent, PublicUserRank punishment) {
      String dateStr = dateToDayAccuracy(punishment.issuedAt);
      List<IElement> rowElements = Collections.list(
          new LabelElement(context, parent).setText(dateStr).setOverflow(TextOverflow.TRUNCATE).setFontScale(super.fontScale).setHorizontalAlignment(HorizontalAlignment.LEFT),
          new LabelElement(context, parent).setText(toSentenceCase(punishment.rank.displayNameNoun)).setOverflow(TextOverflow.TRUNCATE).setFontScale(super.fontScale).setHorizontalAlignment(HorizontalAlignment.LEFT),
          new LabelElement(context, parent).setText(punishment.message).setOverflow(TextOverflow.SPLIT).setFontScale(super.fontScale).setHorizontalAlignment(HorizontalAlignment.LEFT).setSizingMode(SizingMode.FILL),
          new LabelElement(context, parent).setText(punishment.expirationTime == null ? "Yes" : "No").setOverflow(TextOverflow.TRUNCATE).setFontScale(super.fontScale),
          new LabelElement(context, parent).setText(punishment.isActive ? "Yes" : "No").setOverflow(TextOverflow.TRUNCATE).setFontScale(super.fontScale)
      );
      return new RowContents<>(rowElements);
    }
  }

  public static class PunishmentCreateAdapter extends CreateAdapter {
    private final static int MIN_DURATION_TIMEOUT_SECONDS = 60 * 5; // this is the minimum masterchat/youtube timeout period

    @Override
    public boolean shouldIncludeRank(PublicRank rank) {
      return rank.group == RankGroup.PUNISHMENT;
    }

    @Override
    public String getTitle(PublicRank rank) {
      return "Create new " + rank.displayNameNoun;
    }

    @Override
    public String getMessagePlaceholder(PublicRank rank) {
      return toSentenceCase(rank.displayNameNoun) + " reason";
    }

    @Override
    public boolean allowExpiration(RankName rank) {
      return rank != RankName.BAN;
    }

    @Override
    public boolean showClearChatCheckbox(RankName rank) {
      return true;
    }

    @Override
    public String getExpirationSubtitle(PublicRank rank) {
      return rank.name == RankName.TIMEOUT ? "(must be at least 5 minutes)" : String.format("(leave blank for indefinite %s)", rank.displayNameNoun);
    }

    @Override
    public boolean validateExpirationTime(RankName rank, int seconds) {
      int minSeconds = rank == RankName.TIMEOUT ? MIN_DURATION_TIMEOUT_SECONDS : 0;
      return seconds >= minSeconds;
    }
  }

  public static class PunishmentDetailsAdapter extends DetailsAdapter {
    @Override
    public String getHeader(PublicRank rank) {
      return String.format("Details for %s", rank.displayNameNoun.toLowerCase());
    }
  }

  public static class PunishmentChannelRankChangeAdapter extends ChannelRankChangeAdapter {
    public PunishmentChannelRankChangeAdapter() {
      super.internalRankErrorHeaderMessage = "Failed to apply the punishment internally:";
      super.noActionsMessage = "No external punishments were applied.";
      super.actionsHeaderMessage = "External punishments:";
    }

    @Override
    public String getTooltip(@Nonnull PublicRank underlyingRank, @Nullable PublicUserRank punishment, PublicChannelRankChange rankChange) {
      String platform = rankChange.channel.platform == Platform.YOUTUBE ? "YouTube" : "Twitch";
      String punishmentType = underlyingRank.displayNameNoun.toLowerCase();
      if (rankChange.error == null) {
        String actionType = punishment == null || punishment.isActive ? "applied" : "revoked";
        return String.format("Successfully %s %s to %s channel %d.", actionType, punishmentType, platform, rankChange.channel.channelId);
      } else {
        String actionType = punishment == null || punishment.isActive ? "apply" : "revoke";
        return String.format("Failed to %s %s to %s channel %d: %s", actionType, punishmentType, platform, rankChange.channel.channelId, rankChange.error);
      }
    }
  }
}
