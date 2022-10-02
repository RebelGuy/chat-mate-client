package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class RemoveUserRankResponse extends ApiResponseBase<RemoveUserRankResponse.RemoveUserRankResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class RemoveUserRankResponseData {
    public PublicUserRank removedRank;
  }
}
