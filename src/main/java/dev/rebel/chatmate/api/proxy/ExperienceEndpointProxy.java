package dev.rebel.chatmate.api.proxy;

import dev.rebel.chatmate.api.models.experience.GetLeaderboardResponse;
import dev.rebel.chatmate.api.models.experience.GetLeaderboardResponse.GetLeaderboardResponseData;
import dev.rebel.chatmate.api.models.experience.GetRankResponse;
import dev.rebel.chatmate.api.models.experience.GetRankResponse.GetRankResponseData;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceRequest;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse;
import dev.rebel.chatmate.api.models.experience.ModifyExperienceResponse.ModifyExperienceResponseData;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.ApiRequestService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ExperienceEndpointProxy extends EndpointProxy {
  public ExperienceEndpointProxy(LogService logService, ApiRequestService apiRequestService, String basePath) {
    super(logService, apiRequestService, basePath + "/experience");
  }

  public void getLeaderboardAsync(Consumer<GetLeaderboardResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/leaderboard", GetLeaderboardResponse.class, callback, errorHandler);
  }

  public void getRankAsync(@Nonnull Integer channelId, Consumer<GetRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/rank?id=%d", channelId);
    this.makeRequestAsync(Method.GET, url, GetRankResponse.class, callback, errorHandler);
  }

  public void modifyExperienceAsync(@Nonnull ModifyExperienceRequest request, Consumer<ModifyExperienceResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.POST, "/modify", request, ModifyExperienceResponse.class, callback, errorHandler);
  }
}
