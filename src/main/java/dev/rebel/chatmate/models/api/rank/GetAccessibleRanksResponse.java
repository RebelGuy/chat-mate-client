package dev.rebel.chatmate.models.api.rank;

import dev.rebel.chatmate.models.api.rank.GetAccessibleRanksResponse.GetAccessibleRanksResponseData;
import dev.rebel.chatmate.models.publicObjects.rank.PublicRank;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetAccessibleRanksResponse extends ApiResponseBase<GetAccessibleRanksResponseData> {
  @Override
  public Integer GetExpectedSchema() { return 1; }

  public static class GetAccessibleRanksResponseData {
    public PublicRank[] accessibleRanks;
  }
}
