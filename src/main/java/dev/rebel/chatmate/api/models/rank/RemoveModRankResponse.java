package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.publicObjects.rank.PublicChannelRankChange;
import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

import javax.annotation.Nullable;

public class RemoveModRankResponse extends ApiResponseBase<RemoveModRankResponse.RemoveModRankResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class RemoveModRankResponseData {
    public @Nullable PublicUserRank removedRank;
    public @Nullable String removedRankError;
    public PublicChannelRankChange[] channelModChanges;
  }
}
