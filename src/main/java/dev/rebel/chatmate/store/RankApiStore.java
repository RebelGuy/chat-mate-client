package dev.rebel.chatmate.store;

import dev.rebel.chatmate.models.publicObjects.rank.PublicUserRank;
import dev.rebel.chatmate.proxy.RankEndpointProxy;
import dev.rebel.chatmate.services.util.Collections;
import dev.rebel.chatmate.util.LruCache;
import dev.rebel.chatmate.util.Memoiser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class RankApiStore {
  private final RankEndpointProxy rankEndpointProxy;

  private final Set<String> loading;
  private final LruCache<String, List<PublicUserRank>> cache;
  private final Memoiser memoiser;

  public RankApiStore(RankEndpointProxy rankEndpointProxy) {
    this.rankEndpointProxy = rankEndpointProxy;

    this.loading = new HashSet<>();
    this.cache = new LruCache<>(100);
    this.memoiser = new Memoiser();
  }

  // todo: also provide the option to add/remove/update a user rank in response to a server confirmation to a user action, similar to the donation store.
  // that way, we won't have to make a completely new request when we already know what data the server would return
  // todo: add a way to invalidate the cache. either a hotkey combination (ctrl+shift+f5?) or a button in the mod menu
  // todo: add a loading spinner HUD element in top right corner, that also displays an exclamation mark when errors are encountered.
  // clicking that icon will show the list of errors, and individual requests can be retried, or everything can be retried
  // todo: can probably extract some common logic into a Store base class/interface
  /** This should be called whenever an action of ours ends up (or may end up) affecting a user's ranks. */
  public void invalidateUserRanks(int userId) {
    List<String> usersKeys = Collections.filter(this.cache.getKeys(), k -> Objects.equals(k, String.format("%d", userId)));
    this.cache.remove(usersKeys);
  }

  public void loadUserRanks(int userId, Consumer<List<PublicUserRank>> callback, Consumer<Throwable> errorHandler, boolean forceLoad) {
    String key = String.format("%d", userId);
    if (this.cache.has(key) && !forceLoad) {
      callback.accept(this.cache.get(key));

    } else if (this.loading.contains(key)) {
      callback.accept(new ArrayList<>());

    } else {
      this.loading.add(key);
      this.rankEndpointProxy.getRanksAsync(
          userId,
          true,
          res -> {
            this.cache.set(key, Collections.list(res.ranks));
            this.loading.remove(key);
            callback.accept(this.cache.get(key));
          }, err -> {
            this.cache.remove(key);
            this.loading.remove(key);
            errorHandler.accept(err);
          });
    }
  }

  public @Nullable Object getStateToken(int userId) {
    String key = String.format("%d", userId);
    return this.cache.get(key);
  }

  public @Nonnull List<PublicUserRank> getCurrentUserRanks(int userId) {
    String key = String.format("%d", userId);
    if (!this.cache.has(key)) {
      this.loadUserRanks(userId, r -> {}, e -> {}, false);
      return new ArrayList<>();
    }

    List<PublicUserRank> allRanks = this.cache.get(key);
    return this.memoiser.memoise(String.format("%d-current", userId), () -> Collections.filter(allRanks, rank -> rank.isActive), allRanks);
  }

  public @Nonnull List<PublicUserRank> getUserRanksAtTime(int userId, long time) {
    String key = String.format("%d", userId);
    if (!this.cache.has(key)) {
      this.loadUserRanks(userId, r -> {}, e -> {}, false);
      return new ArrayList<>();
    }

    List<PublicUserRank> allRanks = this.cache.get(key);
    return this.memoiser.memoise(String.format("%d-past", userId), () -> {
      return Collections.filter(allRanks, rank -> rank.issuedAt <= time && // rank was issued before the requested time
          (rank.expirationTime == null || rank.expirationTime > time) && // and if it expired, it expired after the requested time
          (rank.revokedAt == null || rank.revokedAt > time) // and if it was revoked, it was revoked after the requested time
      );
    }, allRanks);
  }
}
