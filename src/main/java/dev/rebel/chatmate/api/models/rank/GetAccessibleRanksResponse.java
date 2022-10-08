package dev.rebel.chatmate.api.models.rank;

import dev.rebel.chatmate.api.models.rank.GetAccessibleRanksResponse.GetAccessibleRanksResponseData;
import dev.rebel.chatmate.api.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetAccessibleRanksResponse extends ApiResponseBase<GetAccessibleRanksResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class GetAccessibleRanksResponseData {
    public PublicRank[] accessibleRanks;
  }
}
