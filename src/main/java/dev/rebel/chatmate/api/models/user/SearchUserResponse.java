package dev.rebel.chatmate.api.models.user;

import dev.rebel.chatmate.api.publicObjects.user.PublicUserNames;
import dev.rebel.chatmate.api.proxy.ApiResponseBase;

public class SearchUserResponse extends ApiResponseBase<SearchUserResponse.SearchUserResponseData> {
  @Override
  public Integer GetExpectedSchema() {
    return 4;
  }

  public static class SearchUserResponseData {
    public PublicUserNames[] results;
  }
}
