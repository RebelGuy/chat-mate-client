package dev.rebel.chatmate.services.events;

import dev.rebel.chatmate.services.LogService;
import dev.rebel.chatmate.services.events.models.EventData;

import java.util.function.Function;

public class MinecraftChatEventService extends EventServiceBase<MinecraftChatEventService.ChatEvent> {
  public MinecraftChatEventService(LogService logService) {
    super(ChatEvent.class, logService);
  }

  public void onUpdateChatDimensions(Function<EventData.EventIn, EventData.EventOut> callback, Object key) {
    super.addListener(ChatEvent.UPDATE_CHAT_DIMENSIONS, callback, null, key);
  }

  public void dispatchUpdateChatDimensionsEvent() {
    for (EventHandler<EventData.EventIn, EventData.EventOut, ?> handler : super.getListeners(ChatEvent.UPDATE_CHAT_DIMENSIONS, EventData.Empty.class)) {
      super.safeDispatch(ChatEvent.UPDATE_CHAT_DIMENSIONS, handler, new EventData.EventIn());
    }
  }

  public enum ChatEvent {
    UPDATE_CHAT_DIMENSIONS
  }
}
