package dev.rebel.chatmate.gui;

import dev.rebel.chatmate.gui.hud.*;
import dev.rebel.chatmate.gui.models.DimFactory;
import dev.rebel.chatmate.models.Config;
import dev.rebel.chatmate.services.MinecraftProxyService;
import dev.rebel.chatmate.services.StatusService;
import dev.rebel.chatmate.services.events.ForgeEventService;
import dev.rebel.chatmate.services.events.ServerLogEventService;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** This is the game overlay class - for the focussed menu screen, go to GuiChatMateHudScreen */
public class GuiChatMateHud {
  private final Minecraft minecraft;
  private final DimFactory dimFactory;
  private final ForgeEventService forgeEventService;
  private final StatusService statusService;
  private final FontEngine fontEngine;

//  private final StatusIndicatorComponent statusIndicatorComponent;
  private final LiveViewersComponent liveViewersComponent;
  private final ServerLogsTimeSeriesComponent serverLogsTimeSeriesComponent;

  public final List<IHudComponent> hudComponents;

  public GuiChatMateHud(Minecraft minecraft, FontEngine fontEngine, DimFactory dimFactory, ForgeEventService forgeEventService, StatusService statusService, Config config, ServerLogEventService serverLogEventService) {
    super();
    this.minecraft = minecraft;
    this.dimFactory = dimFactory;
    this.forgeEventService = forgeEventService;
    this.statusService = statusService;
    this.fontEngine = fontEngine;

//    this.statusIndicatorComponent = new StatusIndicatorComponent(dimFactory, 0.5f, statusService, config, serverLogEventService);
    this.liveViewersComponent = new LiveViewersComponent(dimFactory, 1, statusService, config, minecraft, fontEngine);
    this.serverLogsTimeSeriesComponent = new ServerLogsTimeSeriesComponent(dimFactory, serverLogEventService, config);

    this.hudComponents = new ArrayList<>();
//    this.hudComponents.add(this.statusIndicatorComponent);
    this.hudComponents.add(this.liveViewersComponent);
    this.hudComponents.add(this.serverLogsTimeSeriesComponent);
  }

  // render indicators here, etc.
  // will need to check config to see if an indicator should be rendered or not, as well as its transform and content
  public void renderGameOverlay() {
    RenderContext context = new RenderContext(this.minecraft.renderEngine, this.fontEngine);

    for (IHudComponent component : this.hudComponents) {
      component.render(context);
    }
  }
}
