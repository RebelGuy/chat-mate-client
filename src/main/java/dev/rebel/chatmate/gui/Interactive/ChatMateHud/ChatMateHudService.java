package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.config.Config.HudElementTransform;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.events.ServerLogEventService;

import java.util.Map;

import static dev.rebel.chatmate.util.Objects.firstOrNull;

public class ChatMateHudService {
  private final ChatMateHudStore chatMateHudStore;

  public ChatMateHudService(ChatMateHudStore chatMateHudStore, DimFactory dimFactory, Config config, StatusService statusService, ServerLogEventService serverLogEventService) {
    this.chatMateHudStore = chatMateHudStore;

    String mainIndicatorName = "mainIndicator";
    String secondaryIndicatorName = "secondaryIndicator";
    HudElementTransform mainIndicatorTransform = new HudElementTransform(dimFactory.fromGui(10), dimFactory.fromGui(10), 1);
    HudElementTransform secondaryIndicatorTransform = new HudElementTransform(dimFactory.fromGui(10), dimFactory.fromGui(23), 1);

    IndicatorElement.Factory statusIndicatorFactory = new IndicatorElement.Factory(config, statusService, serverLogEventService);
    this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, true, statusIndicatorFactory, config.getStatusIndicatorEmitter(), config.getHudTransformsEmitter(), mainIndicatorTransform, mainIndicatorName));
    this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, false, statusIndicatorFactory, config.getStatusIndicatorEmitter(), config.getHudTransformsEmitter(), secondaryIndicatorTransform, secondaryIndicatorName));

    String mainViewerCountName = "mainViewerCount";
    String secondaryViewerCountName = "secondaryViewerCount";
    HudElementTransform mainViewerCountTransform = new HudElementTransform(dimFactory.fromGui(23), dimFactory.fromGui(10), 1);
    HudElementTransform secondaryViewerCountTransform = new HudElementTransform(dimFactory.fromGui(23), dimFactory.fromGui(23), 1);

    ViewerCountElement.Factory viewerCountFactory = new ViewerCountElement.Factory(config, statusService);
    this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, true, viewerCountFactory, config.getViewerCountEmitter(), config.getHudTransformsEmitter(), mainViewerCountTransform, mainViewerCountName));
    this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, false, viewerCountFactory, config.getViewerCountEmitter(), config.getHudTransformsEmitter(), secondaryViewerCountTransform, secondaryViewerCountName));

    this.chatMateHudStore.addElement((context, parent) -> new ServerLogsTimeSeriesHudElement(context, parent, serverLogEventService, config));
  }
}
