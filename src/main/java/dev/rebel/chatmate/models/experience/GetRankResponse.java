package dev.rebel.chatmate.models.experience;

import dev.rebel.chatmate.models.experience.GetRankResponse.GetRankResponseData;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetRankResponse extends ApiResponseBase<GetRankResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetRankResponseData {
    public Integer relevantIndex;
    public RankedEntry[] entries;
  }
}
