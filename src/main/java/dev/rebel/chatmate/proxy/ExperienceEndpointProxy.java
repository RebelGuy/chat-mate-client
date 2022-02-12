package dev.rebel.chatmate.proxy;

import dev.rebel.chatmate.models.chatMate.GetEventsResponse;
import dev.rebel.chatmate.models.chatMate.GetStatusResponse;
import dev.rebel.chatmate.models.experience.GetLeaderboardResponse;
import dev.rebel.chatmate.models.experience.GetRankResponse;
import dev.rebel.chatmate.services.LogService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.ConnectException;
import java.util.Date;
import java.util.function.Consumer;

public class ExperienceEndpointProxy extends EndpointProxy {
  public ExperienceEndpointProxy(LogService logService, String basePath) {
    super(logService, basePath + "/experience");
  }

  public void getLeaderboardAsync(Consumer<GetLeaderboardResponse> callback, @Nullable Consumer<Throwable> errorHandler) {
    this.makeRequestAsync(Method.GET, "/leaderboard", GetLeaderboardResponse.class, callback, errorHandler);
  }

  public void getRankAsync(@Nonnull String name, Consumer<GetRankResponse> callback, @Nullable Consumer<Throwable> errorHandler) {
    String url = String.format("/rank?name=%s", name);
    this.makeRequestAsync(Method.GET, url, GetRankResponse.class, callback, errorHandler);
  }
}
