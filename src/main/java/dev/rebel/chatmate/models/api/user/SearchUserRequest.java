package dev.rebel.chatmate.models.api.user;

import dev.rebel.chatmate.proxy.ApiRequestBase;

public class SearchUserRequest extends ApiRequestBase {
  public final String searchTerm;

  public SearchUserRequest(String searchTerm) {
    super(4);
    this.searchTerm = searchTerm;
  }
}
