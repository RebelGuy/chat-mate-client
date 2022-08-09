package dev.rebel.chatmate.models.api.rank;

import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class RemoveUserRankResponse extends ApiResponseBase<RemoveUserRankResponse.RemoveUserRankResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class RemoveUserRankResponseData {
    public PublicUserRank removedRank;
  }
}
