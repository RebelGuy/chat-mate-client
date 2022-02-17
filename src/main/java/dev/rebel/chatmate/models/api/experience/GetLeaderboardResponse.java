package dev.rebel.chatmate.models.api.experience;

import dev.rebel.chatmate.models.api.experience.GetLeaderboardResponse.GetLeaderboardResponseData;
import dev.rebel.chatmate.models.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetLeaderboardResponse extends ApiResponseBase<GetLeaderboardResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 2;
  }

  public static class GetLeaderboardResponseData {
    public PublicRankedUser[] rankedUsers;
  }
}
