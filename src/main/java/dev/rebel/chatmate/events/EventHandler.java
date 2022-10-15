package dev.rebel.chatmate.events;

import dev.rebel.chatmate.events.models.EventData;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.function.Function;

// Java doesn't support type inference for classes like it does for methods - e.g. EventService<MyEventData> cannot
// infer MyEventData.MyEventIn, etc. this is why we have to stick to this annoying verbose notation.
/** If no (or a null) key is provided, the event handler is considered STRONG and will hold on to the callback method, which may be a lambda.
 * If a key is provided, the event handler is considered WEAK and will not hold on to the key or callback method, which may NOT be a lambda. */
public class EventHandler<In extends EventData.EventIn, Out extends EventData.EventOut, Options extends EventData.EventOptions> {
  public final Options options;
  private final @Nullable WeakReference<Object> key;

  // It is important that we also don't hold on to the callback reference, otherwise the key may not be released
  // (since they are usually attached to the same class instance)
  private final @Nullable WeakReference<Function<In, Out>> callbackRef;
  private final @Nullable Function<In, Out> callback;

  /** No weak references. */
  public EventHandler(Function<In, Out> callback, Options options) {
    this(callback, options, null);
  }

  /** Weak references. */
  public EventHandler(Function<In, Out> callback, Options options, @Nullable Object key) {
    this.options = options;
    this.callbackRef = key == null ? null : new WeakReference<>(callback);
    this.callback = key == null ? callback : null;
    this.key = key == null ? null : new WeakReference<>(key);
  }

  public @Nullable Function<In, Out> getCallbackRef() {
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
}