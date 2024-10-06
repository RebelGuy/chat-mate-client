package dev.rebel.chatmate.api.models.user;

import dev.rebel.chatmate.api.proxy.ApiResponseBase;
import dev.rebel.chatmate.api.publicObjects.user.PublicUserSearchResults;

public class SearchUserResponse extends ApiResponseBase<SearchUserResponse.SearchUserResponseData> {
  public static class SearchUserResponseData {
    public PublicUserSearchResults[] results;
  }
}
