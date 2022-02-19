package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.api.experience.GetLeaderboardResponse;
import dev.rebel.chatmate.models.api.experience.GetLeaderboardResponse.GetLeaderboardResponseData;
import dev.rebel.chatmate.models.api.experience.GetRankResponse;
import dev.rebel.chatmate.models.api.experience.GetRankResponse.GetRankResponseData;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ExperienceEndpointProxy extends EndpointProxy {
  public ExperienceEndpointProxy(LogService logService, String basePath) {
    super(logService, basePath + "/experience");
  }

  public void getLeaderboardAsync(Consumer<GetLeaderboardResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/leaderboard", GetLeaderboardResponse.class, callback, errorHandler);
  }

  public void getRankAsync(@Nonnull String name, Consumer<GetRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/rank?name=%s", name);
    this.makeRequestAsync(Method.GET, url, GetRankResponse.class, callback, errorHandler);
  }

  public void getRankAsync(@Nonnull Number userId, Consumer<GetRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/rank?id=%s", userId);
    this.makeRequestAsync(Method.GET, url, GetRankResponse.class, callback, errorHandler);
  }

  public void getRankAsync(@Nonnull Integer channelId, Consumer<GetRankResponseData> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/rank?id=%d", channelId);
    this.makeRequestAsync(Method.GET, url, GetRankResponse.class, callback, errorHandler);
  }
}
