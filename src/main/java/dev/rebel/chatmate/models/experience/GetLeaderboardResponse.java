package dev.rebel.chatmate.models.experience;

import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetLeaderboardResponse extends ApiResponseBase {
  public Long timestamp;
  public RankedEntry[] entries;

  @Override
  public Number GetExpectedSchema() {
    return 1;
  }
}
