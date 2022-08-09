package dev.rebel.chatmate.models.api.rank;

import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetUserRanksResponse extends ApiResponseBase<GetUserRanksResponse.GetUserRanksResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class GetUserRanksResponseData {
    public PublicUserRank[] ranks;
  }
}
