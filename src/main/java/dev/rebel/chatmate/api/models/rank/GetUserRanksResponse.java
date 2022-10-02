package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetUserRanksResponse extends ApiResponseBase<GetUserRanksResponse.GetUserRanksResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class GetUserRanksResponseData {
    public PublicUserRank[] ranks;
  }
}
