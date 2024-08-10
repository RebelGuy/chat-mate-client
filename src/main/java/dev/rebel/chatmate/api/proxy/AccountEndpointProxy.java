package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.account.AuthenticateResponse;
import dev.rebel.chatmate.api.models.account.AuthenticateResponse.AuthenticateResponseData;
import dev.rebel.chatmate.api.models.account.LoginRequest;
import dev.rebel.chatmate.api.models.account.LoginResponse;
import dev.rebel.chatmate.api.models.account.LoginResponse.LoginResponseData;
import dev.rebel.chatmate.api.models.account.LogoutResponse;
import dev.rebel.chatmate.api.models.account.LogoutResponse.LogoutResponseData;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class AccountEndpointProxy extends EndpointProxy {
  public AccountEndpointProxy(LogService logService, ApiRequestService apiRequestService, Config config, String basePath) {
    super(logService, apiRequestService, config, basePath + "/account");
  }

  public void authenticateAsync(Consumer<AuthenticateResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/authenticate", AuthenticateResponse.class, callback, errorHandler);
  }

  public void loginAsync(@Nonnull LoginRequest request, Consumer<LoginResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/login", request, LoginResponse.class, callback, errorHandler);
  }

  public void logoutAsync(Consumer<LogoutResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/logout", LogoutResponse.class, callback, errorHandler);
  }
}
