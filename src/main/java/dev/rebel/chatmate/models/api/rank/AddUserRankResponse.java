package dev.rebel.chatmate.models.api.rank;

import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class AddUserRankResponse extends ApiResponseBase<AddUserRankResponse.AddUserRankResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class AddUserRankResponseData {
    public PublicUserRank newRank;
  }
}
