package dev.rebel.chatmate.stores;

import dev.rebel.chatmate.api.proxy.EndpointProxy;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class ApiStore<TData> {
  public static final Long INITIAL_COUNTER = 0L;

  private @Nullable TData data;
  private @Nullable String error;
  private boolean loading;
  private long updateCounter;

  public ApiStore() {
    this.data = null;
    this.error = null;
    this.loading = false;
    this.updateCounter = INITIAL_COUNTER;
  }

  public void clear() {
    this.data = null;
    this.error = null;
    this.loading = false;
    this.updateCounter++;
  }

  public @Nullable String getError() {
    return this.error;
  }

  /** Implement this for actually fetching the data. This is part of the public interface. */
  public abstract void loadData(Consumer<TData> callback, Consumer<Throwable> errorHandler, boolean forceLoad);

  public abstract @Nullable TData getData();

  public long getUpdateCounter() {
    return this.updateCounter;
  }

  protected void loadData(BiConsumer<Consumer<TData>, Consumer<Throwable>> onFetchData, Consumer<TData> callback, Consumer<Throwable> errorHandler, boolean forceLoad) {
    if (this.data != null && !forceLoad) {
      callback.accept(this.data);
      return;
    } else if (this.loading) {
      // todo: only call callback after loading is done
      callback.accept(null);
      return;
    }

    this.loading = true;
    this.updateCounter++;
    onFetchData.accept(data -> {
      this.data = data;
      this.error = null;
      this.loading = false;
      this.updateCounter++;
      callback.accept(data);
    }, err -> {
      this.data = null;
      this.error = EndpointProxy.getApiErrorMessage(err);
      this.loading = false;
      this.updateCounter++;
      errorHandler.accept(err);
    });
  }

  protected @Nullable TData getData(BiConsumer<Consumer<TData>, Consumer<Throwable>> onFetchData) {
    if (this.data == null) {
      if (!this.loading) {
        this.loadData(onFetchData, r -> {}, e -> {}, false);
      }
      return null;
    } else {
      return this.data;
    }
  }
}
