package dev.rebel.chatmate.models.api.user;

import dev.rebel.chatmate.proxy.ApiRequestBase;

public class SearchUserRequest extends ApiRequestBase {
  public final String searchTerm;

  public SearchUserRequest(String searchTerm) {
    super(3);
    this.searchTerm = searchTerm;
  }
}
