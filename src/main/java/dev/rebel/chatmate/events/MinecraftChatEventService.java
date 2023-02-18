package dev.rebel.chatmate.events;

import dev.rebel.chatmate.events.EventHandler.EventCallback;
import dev.rebel.chatmate.services.LogService;

public class MinecraftChatEventService extends EventServiceBase<MinecraftChatEventService.ChatEvent> {
  public MinecraftChatEventService(LogService logService) {
    super(ChatEvent.class, logService);
  }

  public void onUpdateChatDimensions(EventCallback<?> callback, Object key) {
    super.addListener(ChatEvent.UPDATE_CHAT_DIMENSIONS, 0, callback, null, key);
  }

  public void dispatchUpdateChatDimensionsEvent() {
    for (EventHandler<?, ?> handler : super.getListeners(ChatEvent.UPDATE_CHAT_DIMENSIONS)) {
      super.safeDispatch(ChatEvent.UPDATE_CHAT_DIMENSIONS, handler, new Event<>());
    }
  }

  public enum ChatEvent {
    UPDATE_CHAT_DIMENSIONS
  }
}
