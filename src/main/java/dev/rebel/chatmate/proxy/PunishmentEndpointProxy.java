package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.api.punishment.*;
import dev.rebel.chatmate.models.api.punishment.BanUserResponse.BanUserResponseData;
import dev.rebel.chatmate.models.api.punishment.GetPunishmentsResponse.GetPunishmentsResponseData;
import dev.rebel.chatmate.models.api.punishment.UnbanUserResponse.UnbanUserResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.stores.ChatMateEndpointStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PunishmentEndpointProxy extends EndpointProxy {
  public PunishmentEndpointProxy(LogService logService, ChatMateEndpointStore chatMateEndpointStore, String basePath) {
    super(logService, chatMateEndpointStore, basePath + "/punishment");
  }

  public void getPunishmentsAsync(@Nullable Integer userId, @Nullable Boolean activeOnly, Consumer<GetPunishmentsResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/rank?activeOnly=%s", activeOnly == null ? false : activeOnly);
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
}
