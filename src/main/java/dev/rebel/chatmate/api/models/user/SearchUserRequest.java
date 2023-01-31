package dev.rebel.chatmate.api.models.user;

import dev.rebel.chatmate.api.proxy.ApiRequestBase;

public class SearchUserRequest extends ApiRequestBase {
  public final String searchTerm;

  public SearchUserRequest(String searchTerm) {
    this.searchTerm = searchTerm;
  }
}
