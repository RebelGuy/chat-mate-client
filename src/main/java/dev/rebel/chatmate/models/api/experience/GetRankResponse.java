package dev.rebel.chatmate.models.api.experience;

import dev.rebel.chatmate.models.api.experience.GetRankResponse.GetRankResponseData;
import dev.rebel.chatmate.models.publicObjects.user.PublicRankedUser;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class GetRankResponse extends ApiResponseBase<GetRankResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 2;
  }

  public static class GetRankResponseData {
    public Integer relevantIndex;
    public PublicRankedUser[] rankedUsers;
  }
}
