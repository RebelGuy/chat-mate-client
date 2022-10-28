package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.config.Config.HudElementTransform;
import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.gui.models.DimPoint;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.events.ServerLogEventService;
import dev.rebel.chatmate.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.rebel.chatmate.util.Objects.firstOrNull;

public class ChatMateHudService {
  private final ChatMateHudStore chatMateHudStore;
  private final DimFactory dimFactory;
  private final Config config;
  private final StatusService statusService;
  private final ServerLogEventService serverLogEventService;

  private SeparableHudElement mainStatusIndicator;
  private SeparableHudElement secondaryStatusIndicator;
  private SeparableHudElement mainViewerCount;
  private SeparableHudElement secondaryViewerCount;
  private ServerLogsTimeSeriesHudElement serverLogsTimeSeries;

  public ChatMateHudService(ChatMateHudStore chatMateHudStore, DimFactory dimFactory, Config config, StatusService statusService, ServerLogEventService serverLogEventService) {
    this.chatMateHudStore = chatMateHudStore;
    this.dimFactory = dimFactory;
    this.config = config;
    this.statusService = statusService;
    this.serverLogEventService = serverLogEventService;

    this.initialiseStandardElements();
  }

  public void initialiseStandardElements() {
    String mainIndicatorName = "mainIndicator";
    String secondaryIndicatorName = "secondaryIndicator";
    HudElementTransform mainIndicatorTransform = new HudElementTransform(dimFactory.fromGui(10), dimFactory.fromGui(10), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);
    HudElementTransform secondaryIndicatorTransform = new HudElementTransform(dimFactory.fromGui(10), dimFactory.fromGui(23), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);

    IndicatorElement.Factory statusIndicatorFactory = new IndicatorElement.Factory(config, statusService, serverLogEventService);
    this.mainStatusIndicator = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, true, statusIndicatorFactory, config.getStatusIndicatorEmitter(), config.getHudTransformsEmitter(), mainIndicatorTransform, mainIndicatorName));
    this.secondaryStatusIndicator = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, false, statusIndicatorFactory, config.getStatusIndicatorEmitter(), config.getHudTransformsEmitter(), secondaryIndicatorTransform, secondaryIndicatorName));

    String mainViewerCountName = "mainViewerCount";
    String secondaryViewerCountName = "secondaryViewerCount";
    HudElementTransform mainViewerCountTransform = new HudElementTransform(dimFactory.fromGui(23), dimFactory.fromGui(10), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);
    HudElementTransform secondaryViewerCountTransform = new HudElementTransform(dimFactory.fromGui(23), dimFactory.fromGui(23), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);

    ViewerCountElement.Factory viewerCountFactory = new ViewerCountElement.Factory(config, statusService);
    this.mainViewerCount = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, true, viewerCountFactory, config.getViewerCountEmitter(), config.getHudTransformsEmitter(), mainViewerCountTransform, mainViewerCountName));
    this.secondaryViewerCount = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, false, viewerCountFactory, config.getViewerCountEmitter(), config.getHudTransformsEmitter(), secondaryViewerCountTransform, secondaryViewerCountName));

    this.serverLogsTimeSeries = this.chatMateHudStore.addElement((context, parent) -> new ServerLogsTimeSeriesHudElement(context, parent, serverLogEventService, config));
  }

  public void resetHud() {
    List<HudElement> standardElements = Collections.list(
        this.mainStatusIndicator,
        this.secondaryStatusIndicator,
        this.mainViewerCount,
        this.secondaryViewerCount,
        this.serverLogsTimeSeries
    );

    for (HudElement element : standardElements) {
      this.chatMateHudStore.removeElement(element);
    }

    // reset settings
    this.config.getHudEnabledEmitter().set(true);
    this.config.getStatusIndicatorEmitter().set(new Config.SeparableHudElement(true, false, false, PlatformIconPosition.LEFT));
    this.config.getViewerCountEmitter().set(new Config.SeparableHudElement(true, false, false, PlatformIconPosition.LEFT));
    this.config.getShowServerLogsHeartbeat().set(false);
    this.config.getShowServerLogsTimeSeries().set(false);

    // reset transforms
    this.config.getHudTransformsEmitter().set(new HashMap<>());

    // re-instantiate and add to the HudStore
    this.initialiseStandardElements();
  }
}
