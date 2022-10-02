package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

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
