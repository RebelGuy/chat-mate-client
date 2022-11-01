package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.account.*;
import dev.rebel.chatmate.api.models.account.AuthenticateResponse.AuthenticateResponseData;
import dev.rebel.chatmate.api.models.account.LoginResponse.LoginResponseData;
import dev.rebel.chatmate.api.models.account.LogoutResponse.LogoutResponseData;
import dev.rebel.chatmate.api.models.experience.GetLeaderboardResponse;
import dev.rebel.chatmate.api.models.experience.GetLeaderboardResponse.GetLeaderboardResponseData;
import dev.rebel.chatmate.api.models.experience.GetRankResponse;
import dev.rebel.chatmate.api.models.experience.GetRankResponse.GetRankResponseData;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceRequest;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse.ModifyExperienceResponseData;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class AccountEndpointProxy extends EndpointProxy {
  public AccountEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/account");
  }

  public void authenticateAsync(@Nonnull AuthenticateRequest request, Consumer<AuthenticateResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/authenticate", request, AuthenticateResponse.class, callback, errorHandler);
  }

  public void loginAsync(@Nonnull LoginRequest request, Consumer<LoginResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/login", request, LoginResponse.class, callback, errorHandler);
  }

  public void logoutAsync(Consumer<LogoutResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/logout", LogoutResponse.class, callback, errorHandler);
  }
}
