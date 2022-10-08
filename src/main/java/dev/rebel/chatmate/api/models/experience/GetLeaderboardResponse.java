package dev.rebel.chatmate.api.models.experience;

import dev.rebel.chatmate.api.models.experience.GetLeaderboardResponse.GetLeaderboardResponseData;
import dev.rebel.chatmate.api.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class GetLeaderboardResponse extends ApiResponseBase<GetLeaderboardResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 4;
  }

  public static class GetLeaderboardResponseData {
    public PublicRankedUser[] rankedUsers;
  }
}
