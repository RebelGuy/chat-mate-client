package dev.rebel.chatmate.events;
import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class EventServiceBase<EventType extends Enum<EventType>> {
  protected final LogService logService;
  private final Map<EventType, List<EventHandler<?, ?>>> listeners;

  public EventServiceBase(Class<EventType> events, LogService logService) {
    this.logService = logService;
    this.listeners = new HashMap<>();

    for (EventType event : events.getEnumConstants()) {
      this.listeners.put(event, java.util.Collections.synchronizedList(new ArrayList<>()));
    }
  }

  /** Add an event listener without a key (cannot unsubscribe - callback will be held as a strong reference). Lambda allowed. */
  protected final <TData, TOptions> void addListener(EventType event, EventCallback<TData> handler, @Nullable TOptions options) {
    synchronized (this.listeners.get(event)) {
      this.listeners.get(event).add(new EventHandler<>(handler, options));
    }
  }

  /** Add an event listener with a key (can unsubscribe explicitly or implicitly - callback will be held as a weak reference). **LAMBDA FORBIDDEN.** */
  protected final <TData, TOptions> void addListener(EventType event, EventCallback<TData> handler, @Nullable TOptions options, Object key) {
    synchronized (this.listeners.get(event)) {
      if (this.listeners.get(event).stream().anyMatch(eh -> eh.isHandlerForKey(key))) {
        this.logService.logError(this, new RuntimeException("Key already exists for event " + event));
      } else {
        this.listeners.get(event).add(new EventHandler<>(handler, options, key));
      }
    }
    this.removeDeadHandlers(event);
  }

  /** Gets the listeners of an empty event. */
  protected final ArrayList<EventHandler<?, ?>> getListeners(EventType event) {
    this.removeDeadHandlers(event);

    // return a copy of the list
    synchronized (this.listeners.get(event)) {
      return (ArrayList<EventHandler<?, ?>>)Collections.list(this.listeners.get(event));
    }
  }

  /** It is the caller's responsibility to ensure that the correct type parameters are provided. */
  protected final <TData> ArrayList<EventHandler<TData, ?>> getListeners(EventType event, Class<TData> dataClass) {
    this.removeDeadHandlers(event);

    // return a copy of the list
    synchronized (this.listeners.get(event)) {
      return (ArrayList<EventHandler<TData, ?>>)(Object)Collections.list(this.listeners.get(event));
    }
  }

  /** It is the caller's responsibility to ensure that the correct type parameters are provided. */
  protected final <TData, TOptions> ArrayList<EventHandler<TData, TOptions>> getListeners(EventType event, Class<TData> dataClass, Class<TOptions> optionsClass) {
    this.removeDeadHandlers(event);

    // return a copy of the list
    synchronized (this.listeners.get(event)) {
      return (ArrayList<EventHandler<TData, TOptions>>)(Object)Collections.list(this.listeners.get(event));
    }
  }

  protected final boolean removeListener(EventType event, Object key) {
    this.removeDeadHandlers(event);

    synchronized (this.listeners.get(event)) {
      Optional<EventHandler<?, ?>> match = this.listeners.get(event).stream().filter(h -> h.isHandlerForKey(key)).findFirst();

      if (match.isPresent()) {
        this.listeners.get(event).remove(match.get());
        return true;
      } else {
        return false;
      }
    }
  }

  protected final void safeDispatch(EventType eventType, EventHandler<?, ?> handler, Event<?> event) {
    if (!handler.isActive()) {
      this.logService.logWarning(this, "Could not notify listener of the", eventType, "event because it is no longer active.");
      return;
    }

    try {
      ((EventCallback<Object>)handler.getCallbackRef()).dispatch((Event<Object>)event);
    } catch (Exception e) {
      this.logService.logError(this, "A problem occurred while notifying listener of the", event, "event. Event data:", event.getData(), "| Error:", e);
    }
  }

  private void removeDeadHandlers(EventType event) {
    synchronized (this.listeners.get(event)) {
      this.listeners.get(event).removeIf(handler -> !handler.isActive());
    }
  }
}
