package dev.rebel.chatmate.events;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.events.models.EventData;
import dev.rebel.chatmate.events.models.EventData.EventIn;
import dev.rebel.chatmate.events.models.EventData.EventOptions;
import dev.rebel.chatmate.events.models.EventData.EventOut;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

// happy scrolling lmao
public abstract class EventServiceBase<Events extends Enum<Events>> {
  protected final LogService logService;
  private final Map<Events, List<EventHandler<?, ?, ?>>> listeners;

  public EventServiceBase(Class<Events> events, LogService logService) {
    this.logService = logService;
    this.listeners = new HashMap<>();

    for (Events event : events.getEnumConstants()) {
      this.listeners.put(event, java.util.Collections.synchronizedList(new ArrayList<>()));
    }
  }

  /** Add an event listener without a key (cannot unsubscribe - callback will be held as a strong reference). Lambda allowed. */
  protected final <In extends EventIn, Out extends EventOut, Options extends EventOptions> void addListener(Events event, Function<In, Out> handler, Options options) {
    synchronized (this.listeners.get(event)) {
      this.listeners.get(event).add(new EventHandler<>(handler, options));
    }
  }

  /** Add an event listener with a key (can unsubscribe explicitly or implicitly - callback will be held as a weak reference). **LAMBDA FORBIDDEN.** */
  protected final <In extends EventIn, Out extends EventOut, Options extends EventOptions> void addListener(Events event, Function<In, Out> handler, Options options, Object key) {
    synchronized (this.listeners.get(event)) {
      if (this.listeners.get(event).stream().anyMatch(eh -> eh.isHandlerForKey(key))) {
        throw new RuntimeException("Key already exists for event " + event);
      }
      this.listeners.get(event).add(new EventHandler<>(handler, options, key));
    }
    this.removeDeadHandlers(event);
  }

  /** It is the caller's responsibility to ensure that the correct type parameters are provided. */
  protected final <In extends EventIn, Out extends EventOut, Options extends EventOptions, Data extends EventData<In, Out, Options>> ArrayList<EventHandler<In, Out, Options>> getListeners(Events event, Class<Data> eventClass) {
    this.removeDeadHandlers(event);

    // return a copy of the list
    synchronized (this.listeners.get(event)) {
      return (ArrayList<EventHandler<In, Out, Options>>)(Object)Collections.list(this.listeners.get(event));
    }
  }

  protected final boolean removeListener(Events event, Object key) {
    this.removeDeadHandlers(event);

    synchronized (this.listeners.get(event)) {
      Optional<EventHandler<?, ?, ?>> match = this.listeners.get(event).stream().filter(h -> h.isHandlerForKey(key)).findFirst();

      if (match.isPresent()) {
        this.listeners.get(event).remove(match.get());
        return true;
      } else {
        return false;
      }
    }
  }

  protected final @Nullable <In extends EventIn, Out extends EventOut, Options extends EventOptions> Out safeDispatch(Events event, EventHandler<In, Out, Options> handler, In eventIn) {
    if (!handler.isActive()) {
      this.logService.logWarning(this, "Could not notify listener of the", event, "event because it is no longer active.");
      return null;
    }

    try {
      return handler.getCallbackRef().apply(eventIn);
    } catch (Exception e) {
      this.logService.logError(this, "A problem occurred while notifying listener of the", event, "event. Event data:", eventIn, "| Error:", e);
      return null;
    }
  }

  private void removeDeadHandlers(Events event) {
    synchronized (this.listeners.get(event)) {
      this.listeners.get(event).removeIf(handler -> !handler.isActive());
    }
  }
}
