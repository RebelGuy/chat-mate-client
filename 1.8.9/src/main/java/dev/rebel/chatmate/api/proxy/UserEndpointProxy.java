package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.user.SearchUserRequest;
import dev.rebel.chatmate.api.models.user.SearchUserResponse;
import dev.rebel.chatmate.api.models.user.SearchUserResponse.SearchUserResponseData;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.ApiRequestService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class UserEndpointProxy extends EndpointProxy {
  public UserEndpointProxy(LogService logService, ApiRequestService apiRequestService, Config config, String basePath) {
    super(logService, apiRequestService, config, basePath + "/user");
  }

  public void searchUser(@Nonnull SearchUserRequest searchRequest, Consumer<SearchUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/search", searchRequest, SearchUserResponse.class, callback, errorHandler);
  }
}
