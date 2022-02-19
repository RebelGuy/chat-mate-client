package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.api.user.SearchUserRequest;
import dev.rebel.chatmate.models.api.user.SearchUserResponse;
import dev.rebel.chatmate.models.api.user.SearchUserResponse.SearchUserResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.stores.ChatMateEndpointStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class UserEndpointProxy extends EndpointProxy {
  public UserEndpointProxy(LogService logService, ChatMateEndpointStore chatMateEndpointStore, String basePath) {
    super(logService, chatMateEndpointStore, basePath + "/user");
  }

  public void searchUser(@Nonnull SearchUserRequest searchRequest, Consumer<SearchUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/search", searchRequest, SearchUserResponse.class, callback, errorHandler);
  }
}
