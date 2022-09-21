package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.events.ServerLogEventService;

public class ChatMateHudService {
  private final ChatMateHudStore chatMateHudStore;

  public ChatMateHudService(ChatMateHudStore chatMateHudStore, Config config, StatusService statusService, ServerLogEventService serverLogEventService) {
    this.chatMateHudStore = chatMateHudStore;

    this.chatMateHudStore.addElement((context, parent) -> new StatusIndicatorHudElement(context, parent, statusService, config, serverLogEventService));
    this.chatMateHudStore.addElement((context, parent) -> new LiveViewersHudElement(context, parent, statusService, config));
    this.chatMateHudStore.addElement((context, parent) -> new ServerLogsTimeSeriesHudElement(context, parent, serverLogEventService, config));
  }
}
