package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.rank.*;
import dev.rebel.chatmate.api.models.rank.AddModRankResponse.AddModRankResponseData;
import dev.rebel.chatmate.api.models.rank.AddUserRankResponse.AddUserRankResponseData;
import dev.rebel.chatmate.api.models.rank.GetAccessibleRanksResponse.GetAccessibleRanksResponseData;
import dev.rebel.chatmate.api.models.rank.GetUserRanksResponse.GetUserRanksResponseData;
import dev.rebel.chatmate.api.models.rank.RemoveModRankResponse.RemoveModRankResponseData;
import dev.rebel.chatmate.api.models.rank.RemoveUserRankResponse.RemoveUserRankResponseData;
import dev.rebel.chatmate.services.ApiRequestService;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class RankEndpointProxy extends EndpointProxy {
  public RankEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/rank");
  }

  public void getRanksAsync(int userId, @Nullable Boolean includeInactive, Consumer<GetUserRanksResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String path = String.format("?userId=%d&includeInactive=%s", userId, includeInactive == null ? false : includeInactive);
    this.makeRequestAsync(Method.GET, path, GetUserRanksResponse.class, callback, errorHandler);
  }

  public void getAccessibleRanksAsync(Consumer<GetAccessibleRanksResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/accessible", GetAccessibleRanksResponse.class, callback, errorHandler);
  }

  public void addUserRank(AddUserRankRequest request, Consumer<AddUserRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "", request, AddUserRankResponse.class, callback, errorHandler);
  }

  public void removeUserRank(RemoveUserRankRequest request, Consumer<RemoveUserRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.DELETE, "", request, RemoveUserRankResponse.class, callback, errorHandler);
  }

  public void addModRank(AddModRankRequest request, Consumer<AddModRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/mod", request, AddModRankResponse.class, callback, errorHandler);
  }

  public void removeModRank(RemoveModRankRequest request, Consumer<RemoveModRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.DELETE, "/mod", request, RemoveModRankResponse.class, callback, errorHandler);
  }
}
