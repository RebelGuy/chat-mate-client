package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.punishment.*;
import dev.rebel.chatmate.api.models.punishment.BanUserResponse.BanUserResponseData;
import dev.rebel.chatmate.api.models.punishment.GetPunishmentsResponse.GetPunishmentsResponseData;
import dev.rebel.chatmate.api.models.punishment.GetSinglePunishmentResponse.GetSinglePunishmentResponseData;
import dev.rebel.chatmate.api.models.punishment.MuteUserResponse.MuteUserResponseData;
import dev.rebel.chatmate.api.models.punishment.RevokeTimeoutResponse.RevokeTimeoutResponseData;
import dev.rebel.chatmate.api.models.punishment.TimeoutUserResponse.TimeoutUserResponseData;
import dev.rebel.chatmate.api.models.punishment.UnbanUserResponse.UnbanUserResponseData;
import dev.rebel.chatmate.api.models.punishment.UnmuteUserResponse.UnmuteUserResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.ApiRequestService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PunishmentEndpointProxy extends EndpointProxy {
  public PunishmentEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/punishment");
  }

  public void getSinglePunishmentAsync(int punishmentId, Consumer<GetSinglePunishmentResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/%d", punishmentId);
    this.makeRequestAsync(Method.GET, url, GetSinglePunishmentResponse.class, callback, errorHandler);
  }

  public void getPunishmentsAsync(@Nullable Integer userId, Boolean includeInactive, Consumer<GetPunishmentsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("?includeInactive=%s", includeInactive);
    if (userId != null) {
      url += String.format("&userId=%d", userId);
    }
    this.makeRequestAsync(Method.GET, url, GetPunishmentsResponse.class, callback, errorHandler);
  }

  public void banUserAsync(@Nonnull BanUserRequest request, Consumer<BanUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/ban", request, BanUserResponse.class, callback, errorHandler);
  }

  public void unbanUserAsync(@Nonnull UnbanUserRequest request, Consumer<UnbanUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/unban", request, UnbanUserResponse.class, callback, errorHandler);
  }

  public void timeoutUserAsync(@Nonnull TimeoutUserRequest request, Consumer<TimeoutUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/timeout", request, TimeoutUserResponse.class, callback, errorHandler);
  }

  public void revokeTimeoutAsync(@Nonnull RevokeTimeoutRequest request, Consumer<RevokeTimeoutResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/revokeTimeout", request, RevokeTimeoutResponse.class, callback, errorHandler);
  }

  public void muteUserAsync(@Nonnull MuteUserRequest request, Consumer<MuteUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/mute", request, MuteUserResponse.class, callback, errorHandler);
  }

  public void unmuteUserAsync(@Nonnull UnmuteUserRequest request, Consumer<UnmuteUserResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/unmute", request, UnmuteUserResponse.class, callback, errorHandler);
  }
}
