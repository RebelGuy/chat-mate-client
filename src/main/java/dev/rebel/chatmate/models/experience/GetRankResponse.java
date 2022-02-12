package dev.rebel.chatmate.models.experience;

import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetRankResponse extends ApiResponseBase {
  public Long timestamp;
  public Integer relevantIndex;
  public RankedEntry[] entries;

  @Override
  public Number GetExpectedSchema() {
    return 1;
  }
}
