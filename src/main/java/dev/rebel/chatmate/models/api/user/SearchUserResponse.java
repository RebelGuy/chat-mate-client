package dev.rebel.chatmate.models.api.user;

import dev.rebel.chatmate.models.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.proxy.ApiResponseBase;

public class SearchUserResponse extends ApiResponseBase<SearchUserResponse.SearchUserResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 3;
  }

  public static class SearchUserResponseData {
    public PublicUserNames[] results;
  }
}
