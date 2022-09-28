package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.events.ServerLogEventService;

public class ChatMateHudService {
  private final ChatMateHudStore chatMateHudStore;

  public ChatMateHudService(ChatMateHudStore chatMateHudStore, DimFactory dimFactory, Config config, StatusService statusService, ServerLogEventService serverLogEventService) {
    this.chatMateHudStore = chatMateHudStore;

    IndicatorElement.Factory statusIndicatorFactory = new IndicatorElement.Factory(config, statusService, serverLogEventService);
    DimPoint defaultStatusIndicatorPosition = new DimPoint(dimFactory.fromGui(10), dimFactory.fromGui(10));
    SeparableHudElement mainIndicator = this.chatMateHudStore.addElement((context, parent) ->
        new SeparableHudElement(context, parent, null, statusIndicatorFactory, config.getStatusIndicatorEmitter(), defaultStatusIndicatorPosition)
    );
    this.chatMateHudStore.addElement((context, parent) ->
        new SeparableHudElement(context, parent, mainIndicator, statusIndicatorFactory, config.getStatusIndicatorEmitter(), defaultStatusIndicatorPosition)
    );

    this.chatMateHudStore.addElement((context, parent) -> new LiveViewersHudElement(context, parent, statusService, config));
    this.chatMateHudStore.addElement((context, parent) -> new ServerLogsTimeSeriesHudElement(context, parent, serverLogEventService, config));
  }
}
