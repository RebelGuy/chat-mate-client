package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.events.models.ConfigEventOptions;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class ApiStore<TData> {
  private final static long ERROR_TIMEOUT_MS = 10_000; // hardcoded for now - see CHAT-803
  public static final Long INITIAL_COUNTER = 0L;

  private final Config config;
  private final boolean requiresLogin;

  private @Nullable CopyOnWriteArrayList<TData> data;
  private @Nullable Throwable error;
  private @Nullable Long errorExpiry;
  private boolean loading;
  private long updateCounter;

  public ApiStore(Config config, boolean requiresLogin) {
    this.config = config;
    this.requiresLogin = requiresLogin;

    this.data = null;
    this.error = null;
    this.errorExpiry = null;
    this.loading = false;
    this.updateCounter = INITIAL_COUNTER;

    config.getLoginInfoEmitter().onChange(_info -> this.clear(), new ConfigEventOptions<>(info -> info.loginToken == null));
  }

  public void clear() {
    this.data = null;
    this.error = null;
    this.errorExpiry = null;
    this.loading = false;
    this.updateCounter++;
  }

  /** Returns an object with the same reference across multiple calls, unless any of the data has changed. */
  public @Nullable List<TData> getData() {
    if (this.data == null) {
      if (!this.loading) {
        this.loadData(null, null, false);
      }
      return new ArrayList<>();
    } else {
      return this.data;
    }
  }

  public @Nullable String getError() {
    if (this.error == null) {
      return null;
    }

    assert this.errorExpiry != null;
    return new Date().getTime() < this.errorExpiry ? EndpointProxy.getApiErrorMessage(this.error) : null;
  }

  public long getUpdateCounter() {
    return this.updateCounter;
  }

  public boolean isLoading() {
    return this.loading;
  }

  public void retry() {
    this.loadData(null, null, true);
  }

  /** By default, the data won't be reloaded if there is an active error unless `forceLoad` is true. */
  public void loadData(@Nullable Consumer<List<TData>> callback, @Nullable Consumer<Throwable> errorHandler, boolean forceLoad) {
    if (this.requiresLogin && this.config.getLoginInfoEmitter().get().username == null) {
      return;
    }

    if (this.data != null && !forceLoad) {
      if (callback != null) {
        callback.accept(this.data);
      }
      return;
    } else if (this.getError() != null && !forceLoad) {
      if (errorHandler != null) {
        errorHandler.accept(this.error);
      }
      return;
    } else if (this.loading) {
      // todo: only call callback after loading is done
      if (callback != null) {
        callback.accept(new ArrayList<>());
      }
      return;
    }

    this.loading = true;
    this.updateCounter++;
    this.onFetchData(res -> {
      this.data = new CopyOnWriteArrayList<>(res);
      this.error = null;
      this.errorExpiry = null;
      this.loading = false;
      this.updateCounter++;
      if (callback != null) {
        callback.accept(this.data);
      }
    }, err -> {
      this.data = null;
      this.error = err;
      this.errorExpiry = new Date().getTime() + ERROR_TIMEOUT_MS;
      this.loading = false;
      this.updateCounter++;
      if (errorHandler != null) {
        errorHandler.accept(err);
      }
    }, forceLoad);
  }

  public void addOne(TData item) {
    if (this.data == null || item == null) {
      this.data = new CopyOnWriteArrayList<>(Collections.list(item));
    } else {
      // copy the collection so the reference changes
      List<TData> list = Collections.list(this.data);
      list.add(item);
      this.data = new CopyOnWriteArrayList<>(list);
    }

    this.updateCounter++;
  }

  public void updateOne(TData updatedItem) {
    if (this.data == null || updatedItem == null) {
      this.data = new CopyOnWriteArrayList<>(Collections.list(updatedItem));
    } else {
      // copy the collection so the reference changes
      this.data = new CopyOnWriteArrayList<>(
          Collections.replaceOne(Collections.list(this.data), updatedItem, oldItem -> this.onMatchItems(oldItem, updatedItem))
      );
    }

    this.updateCounter++;
  }

  public void deleteOne(TData item) {
    if (this.data == null || item == null) {
      return;
    } else {
      // copy the collection so the reference changes
      this.data = new CopyOnWriteArrayList<>(
          Collections.filter(Collections.list(this.data), oldItem -> !this.onMatchItems(oldItem, item))
      );
    }

    this.updateCounter++;
  }

  protected abstract void onFetchData(Consumer<List<TData>> onData, Consumer<Throwable> onError, boolean isActiveRequest);
  protected abstract boolean onMatchItems(TData a, TData b);
}
