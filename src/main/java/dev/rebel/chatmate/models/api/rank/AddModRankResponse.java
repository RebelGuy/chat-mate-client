package dev.rebel.chatmate.models.api.rank;

import dev.rebel.chatmate.models.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class AddModRankResponse extends ApiResponseBase<AddModRankResponse.AddModRankResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class AddModRankResponseData {
    public @Nullable PublicUserRank newRank;
    public @Nullable String newRankError;
    public PublicChannelRankChange[] channelModChanges;
  }
}
