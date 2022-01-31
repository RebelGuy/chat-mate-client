package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.services.events.models.EventData;

import java.util.function.Function;

// Java doesn't support type inference for classes like it does for methods - e.g. EventService<MyEventData> cannot
// infer MyEventData.MyEventIn, etc. this is why we have to stick to this annoying verbose notation.
public class EventHandler<In extends EventData.EventIn, Out extends EventData.EventOut, Options extends EventData.EventOptions> {
  private final Object key;
  public final Function<In, Out> callback;
  public final Options options;

  public EventHandler(Function<In, Out> callback, Options options) {
    this.callback = callback;
    this.options = options;
    this.key = new Object();
  }

  public EventHandler(Function<In, Out> callback, Options options, Object key) {
    this.callback = callback;
    this.options = options;
    this.key = key;
  }

  // we can't compare by callback because lambdas cannot be expected to follow reference equality.
  public boolean isHandlerForKey(Object key) {
    return this.key == key;
  }
}
