package dev.rebel.chatmate.services.events;
import dev.rebel.chatmate.services.events.models.EventData;
import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOptions;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

// happy scrolling lmao
public abstract class EventServiceBase<Events extends Enum<Events>> {
  private Map<Events, ArrayList<EventHandler<?, ?, ?>>> listeners;

  public EventServiceBase(Class<Events> events) {
    this.listeners = new HashMap<>();

    for (Events event : events.getEnumConstants()) {
      this.listeners.put(event, new ArrayList<>());
    }
  }

  /** Add an event listener without a key (cannot unsubscribe) */
  protected final <In extends EventIn, Out extends EventOut, Options extends EventOptions> void addListener(Events event, Function<In, Out> handler, Options options) {
    this.listeners.get(event).add(new EventHandler<>(handler, options));
  }

  /** Add an event listener with a key (can unsubscribe) */
  protected final <In extends EventIn, Out extends EventOut, Options extends EventOptions> void addListener(Events event, Function<In, Out> handler, Options options, Object key) {
    this.listeners.get(event).add(new EventHandler<>(handler, options, key));
  }

  /** It is the caller's responsibility to ensure that the correct type parameters are provided. */
  protected final <In extends EventIn, Out extends EventOut, Options extends EventOptions, Data extends EventData<In, Out, Options>> ArrayList<EventHandler<In, Out, Options>> getListeners(Events event, Class<Data> eventClass) {
    return (ArrayList<EventHandler<In, Out, Options>>)(Object)this.listeners.get(event);
  }

  protected final boolean removeListener(Events event, Object key) {
    Optional<EventHandler<?, ?, ?>> match = this.listeners.get(event).stream().filter(h -> h.isHandlerForKey(key)).findFirst();

    if (match.isPresent()) {
      this.listeners.get(event).remove(match.get());
      return true;
    } else {
      return false;
    }
  }
}
