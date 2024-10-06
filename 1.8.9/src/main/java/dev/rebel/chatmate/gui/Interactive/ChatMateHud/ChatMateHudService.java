package dev.rebel.chatmate.gui.Interactive.ChatMateHud;

import dev.rebel.chatmate.config.Config.HudElementTransform;
import dev.rebel.chatmate.config.Config.SeparableHudElement.PlatformIconPosition;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.config.Config;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.util.Collections;

import java.util.HashMap;
import java.util.List;

public class ChatMateHudService {
  private final ChatMateHudStore chatMateHudStore;
  private final DimFactory dimFactory;
  private final Config config;
  private final StatusService statusService;

  private SeparableHudElement mainStatusIndicator;
  private SeparableHudElement secondaryStatusIndicator;
  private SeparableHudElement mainViewerCount;
  private SeparableHudElement secondaryViewerCount;

  public ChatMateHudService(ChatMateHudStore chatMateHudStore, DimFactory dimFactory, Config config, StatusService statusService) {
    this.chatMateHudStore = chatMateHudStore;
    this.dimFactory = dimFactory;
    this.config = config;
    this.statusService = statusService;

    this.initialiseStandardElements();
  }

  public void initialiseStandardElements() {
    String mainIndicatorName = "mainIndicator";
    String secondaryIndicatorName = "secondaryIndicator";
    HudElementTransform mainIndicatorTransform = new HudElementTransform(dimFactory.fromGui(10), dimFactory.fromGui(10), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);
    HudElementTransform secondaryIndicatorTransform = new HudElementTransform(dimFactory.fromGui(10), dimFactory.fromGui(23), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);

    IndicatorElement.Factory statusIndicatorFactory = new IndicatorElement.Factory(config, statusService);
    this.mainStatusIndicator = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, true, statusIndicatorFactory, config.getStatusIndicatorEmitter(), config.getHudTransformsEmitter(), config.getOnlyShowIndicatorsWhenLive(), mainIndicatorTransform, mainIndicatorName));
    this.secondaryStatusIndicator = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, false, statusIndicatorFactory, config.getStatusIndicatorEmitter(), config.getHudTransformsEmitter(), config.getOnlyShowIndicatorsWhenLive(), secondaryIndicatorTransform, secondaryIndicatorName));

    String mainViewerCountName = "mainViewerCount";
    String secondaryViewerCountName = "secondaryViewerCount";
    HudElementTransform mainViewerCountTransform = new HudElementTransform(dimFactory.fromGui(23), dimFactory.fromGui(10), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);
    HudElementTransform secondaryViewerCountTransform = new HudElementTransform(dimFactory.fromGui(23), dimFactory.fromGui(23), HudElement.Anchor.TOP_LEFT, dimFactory.getMinecraftRect(), dimFactory.getScaleFactor(), 1);

    ViewerCountElement.Factory viewerCountFactory = new ViewerCountElement.Factory(config, statusService);
    this.mainViewerCount = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, true, viewerCountFactory, config.getViewerCountEmitter(), config.getHudTransformsEmitter(), config.getOnlyShowIndicatorsWhenLive(), mainViewerCountTransform, mainViewerCountName));
    this.secondaryViewerCount = this.chatMateHudStore.addElement((context, parent) -> new SeparableHudElement(context, parent, false, viewerCountFactory, config.getViewerCountEmitter(), config.getHudTransformsEmitter(), config.getOnlyShowIndicatorsWhenLive(), secondaryViewerCountTransform, secondaryViewerCountName));
  }

  public void resetHud() {
    List<HudElement> standardElements = Collections.list(
        this.mainStatusIndicator,
        this.secondaryStatusIndicator,
        this.mainViewerCount,
        this.secondaryViewerCount
    );

    for (HudElement element : standardElements) {
      this.chatMateHudStore.removeElement(element);
    }

    // reset settings
    this.config.getHudEnabledEmitter().set(true);
    this.config.getStatusIndicatorEmitter().set(new Config.SeparableHudElement(true, false, false, PlatformIconPosition.LEFT));
    this.config.getViewerCountEmitter().set(new Config.SeparableHudElement(true, false, false, PlatformIconPosition.LEFT));

    // reset transforms
    this.config.getHudTransformsEmitter().set(new HashMap<>());

    // re-instantiate and add to the HudStore
    this.initialiseStandardElements();
  }
}
