package dev.rebel.chatmate.models.experience;

import dev.rebel.chatmate.models.experience.GetLeaderboardResponse.GetLeaderboardResponseData;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetLeaderboardResponse extends ApiResponseBase<GetLeaderboardResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 1;
  }

  public static class GetLeaderboardResponseData {
    public RankedEntry[] entries;
  }
}
