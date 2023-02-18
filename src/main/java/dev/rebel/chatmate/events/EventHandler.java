package dev.rebel.chatmate.events;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/** If no (or a null) key is provided, the event handler is considered STRONG and will hold on to the callback method, which may be a lambda.
 * If a key is provided, the event handler is considered WEAK and will not hold on to the key or callback method, which may NOT be a lambda. */
public class EventHandler<TData, TOptions> {
  public final int zIndex;
  public final TOptions options;
  private final @Nullable WeakReference<Object> key;

  // It is important that we also don't hold on to the callback reference, otherwise the key may not be released
  // (since they are usually attached to the same class instance)
  private final @Nullable WeakReference<EventCallback<TData>> callbackRef;
  private final @Nullable EventCallback<TData> callback;

  /** No weak references. */
  public EventHandler(int zIndex, EventCallback<TData> callback, TOptions options) {
    this(zIndex, callback, options, null);
  }

  /** Weak references. */
  public EventHandler(int zIndex, EventCallback<TData> callback, TOptions options, @Nullable Object key) {
    this.zIndex = zIndex;
    this.options = options;
    this.callbackRef = key == null ? null : new WeakReference<>(callback);
    this.callback = key == null ? callback : null;
    this.key = key == null ? null : new WeakReference<>(key);
  }

  public @Nullable EventCallback<TData> getCallbackRef() {
    return this.key == null ? this.callback : this.callbackRef.get();
  }

  // we can't compare by callback because lambdas cannot be expected to follow reference equality.
  public boolean isHandlerForKey(Object key) {
    return this.key != null && this.key.get() == key;
  }

  /** If `isActive` is true, `getCallbackRef` is guaranteed to return a non-null value. */
  public boolean isActive() {
    if (this.key == null) {
      return true;
    } else if (this.key.get() == null || this.callbackRef.get() == null) {
      return false;
    } else {
      return true;
    }
  }

  @FunctionalInterface
  public interface EventCallback<TData> {
    void dispatch(Event<TData> event);
  }
}
