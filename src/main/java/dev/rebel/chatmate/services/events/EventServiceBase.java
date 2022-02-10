package dev.rebel.chatmate.services.events;
import dev.rebel.chatmate.models.chatMate.GetEventsResponse.EventType;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.models.EventData;
import dev.rebel.chatmate.services.events.models.EventData.EventIn;
import dev.rebel.chatmate.services.events.models.EventData.EventOptions;
import dev.rebel.chatmate.services.events.models.EventData.EventOut;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

// happy scrolling lmao
public abstract class EventServiceBase<Events extends Enum<Events>> {
  private final LogService logService;
  private final Map<Events, ArrayList<EventHandler<?, ?, ?>>> listeners;

  public EventServiceBase(Class<Events> events, LogService logService) {
    this.logService = logService;
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
    if (this.listeners.get(event).stream().anyMatch(eh -> eh.isHandlerForKey(key))) {
      throw new RuntimeException("Key already exists for event " + event);
    }
    this.listeners.get(event).add(new EventHandler<>(handler, options, key));
    this.removeDeadHandlers(event);
  }

  /** It is the caller's responsibility to ensure that the correct type parameters are provided. */
  protected final <In extends EventIn, Out extends EventOut, Options extends EventOptions, Data extends EventData<In, Out, Options>> ArrayList<EventHandler<In, Out, Options>> getListeners(Events event, Class<Data> eventClass) {
    this.removeDeadHandlers(event);
    return (ArrayList<EventHandler<In, Out, Options>>)(Object)this.listeners.get(event);
  }

  protected final boolean removeListener(Events event, Object key) {
    this.removeDeadHandlers(event);
    Optional<EventHandler<?, ?, ?>> match = this.listeners.get(event).stream().filter(h -> h.isHandlerForKey(key)).findFirst();

    if (match.isPresent()) {
      this.listeners.get(event).remove(match.get());
      return true;
    } else {
      return false;
    }
  }

  protected final @Nullable <In extends EventIn, Out extends EventOut, Options extends EventOptions> Out safeDispatch(Events event, EventHandler<In, Out, Options> handler, In eventIn) {
    try {
      return handler.callback.apply(eventIn);
    } catch (Exception e) {
      this.logService.logError(this, "A problem occurred while notifying listener of the", EventType.LEVEL_UP, "event. Event data:", eventIn);
      return null;
    }
  }

  private void removeDeadHandlers(Events event) {
    this.listeners.get(event).removeIf(handler -> !handler.isActive());
  }
}
